package com.zane.breakpadandroid;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chodison.mybreakpad.DumpProcessor;
import com.chodison.mybreakpad.ExceptionHandler;
import com.chodison.mybreakpad.NativeMybreakpad;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.functions.Consumer;
import io.reactivex.schedulers.Schedulers;

/**
 * 说明：
 *      1、点击 crash 制造 crash (app 闪退)
 *      2、点击 process 解析
 *
 *      3、重新进入 app 之后检查是否存在 dump 文件, 存在则解析并"上传文件"(<-假装的啊, 现在木有后台), 随后删除文件 (待定)
 *
 * 作者：zhouzhan
 * 日期：17/8/15 14:14
 */
public class MainActivity extends AppCompatActivity {

    private static final String SDCARD_DIR = Environment.getExternalStorageDirectory().getAbsolutePath();
    private static final String DUMP_DIR = SDCARD_DIR +"/dumps";
    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String[] app_so = {"libtest1.so","libmybreakpad.so","libtest2.so"};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        checkDir();
//        ExceptionHandler.init(DUMP_DIR);
        NativeMybreakpad.init(DUMP_DIR);
    }

    public void doClick(View view){
        int id = view.getId();
        switch (id){
            case R.id.bt_crash:
//                ExceptionHandler.testNativeCrash();
                NativeMybreakpad.testNativeCrash();
                break;
            case R.id.bt_processor:
                doProcess();
                break;
        }
    }

    /**
     * 解析 dump 文件
     */
    private void doProcess() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {
                File dir = new File(DUMP_DIR);
                File[] files = dir.listFiles();
                for (File file : files) {
                    String fileName = file.getName();
                    String lastName = ".dmp";
                    int lastIndexOf = fileName.lastIndexOf(lastName);

                    if (lastIndexOf + lastName.length() == fileName.length()) { // 说明 .dmp 是后缀名
                        String dumpPath = file.getAbsolutePath();
                        String crashFileName = DUMP_DIR + "/crash.txt";
//                        boolean exec = DumpProcessor.exec(new String[]{"minidump_stackwalk", dumpPath}, crashFileName);
                        boolean exec = NativeMybreakpad.dumpFileProcess(dumpPath, crashFileName, app_so);
//                        if (exec) { // 解析完成之后 删除 dmp 文件
//                            boolean isDelete = file.delete();
//                            Log.e(TAG, "isDelete: " + isDelete);
//                        }
                        e.onNext(exec);
                    }
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.e(TAG, "processed: " + aBoolean);
                        if(aBoolean) {
                            String[] crashSoName = NativeMybreakpad.getCrashSoName();
                            for(int i = 0; i < crashSoName.length; i ++) {
                                Log.e(TAG, "crash so name["+i+"]: " + crashSoName[i].toString());
                            }
                        }
                    }
                });
    }


    /**
     * 检查是否存在 dump 文件夹, 木有则创建
     */
    private void checkDir() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {
                File sdCardDir = new File(SDCARD_DIR);
                if (!sdCardDir.exists()) {
                    e.onNext(false);
                    return;
                }
                File dir = new File(DUMP_DIR);
                if (dir.exists() && dir.isDirectory()){
                    e.onNext(true);
                    return;
                }
                // 创建文件夹
                boolean mkdir = dir.mkdir();
                e.onNext(mkdir);
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Consumer<Boolean>() {
                    @Override
                    public void accept(Boolean aBoolean) throws Exception {
                        Log.e(TAG, "checkDir: " + aBoolean);
                    }
                });
    }
}
