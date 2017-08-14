package com.zane.breakpadandroid;

import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.chodison.mybreakpad.DumpProcessor;
import com.chodison.mybreakpad.ExceptionHandler;

import java.io.File;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.annotations.NonNull;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;

public class MainActivity extends AppCompatActivity {

    private static final String DUMP_DIR = Environment.getExternalStorageDirectory().getAbsolutePath()+"/dumps";
    private static final String TAG = MainActivity.class.getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // TODO: 17/8/11 for test
        ExceptionHandler.init(DUMP_DIR);
    }

    public void doClick(View view){

        int id = view.getId();
        switch (id){
            case R.id.bt_crash:

                ExceptionHandler.testNativeCrash();
                break;
            case R.id.bt_processor:
                doProcess();
                break;
        }


    }

    private void doProcess() {
        Observable.create(new ObservableOnSubscribe<Boolean>() {
            @Override
            public void subscribe(@NonNull ObservableEmitter<Boolean> e) throws Exception {
                File dir = new File(DUMP_DIR);
                File[] files = dir.listFiles();
                for (File file : files){
                    String dumpPath = file.getAbsolutePath();
                    String crashFileName = DUMP_DIR + "/crash.txt";
                    boolean exec = DumpProcessor.exec(new String[]{crashFileName, dumpPath});
                    e.onNext(exec);
                }
            }
        }).subscribeOn(Schedulers.newThread())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new Observer<Boolean>() {
                    @Override
                    public void onSubscribe(@NonNull Disposable d) {

                    }

                    @Override
                    public void onNext(@NonNull Boolean b) {
                        Log.e(TAG, "processed: " + b);
                    }

                    @Override
                    public void onError(@NonNull Throwable e) {

                    }

                    @Override
                    public void onComplete() {

                    }
                });
    }
}
