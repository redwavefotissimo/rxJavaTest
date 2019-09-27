package com.android.personal.myapplication;

import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;

import android.os.Bundle;

import com.android.personal.myapplication.databinding.ActivityMainTestBinding;
import com.jakewharton.rxbinding3.widget.RxTextView;
import com.jakewharton.rxbinding3.widget.TextViewTextChangeEvent;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.ObservableOnSubscribe;
import io.reactivex.Observer;
import io.reactivex.android.schedulers.AndroidSchedulers;
//import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;


public class MainTestActivity extends AppCompatActivity {

    ActivityMainTestBinding binding;
    Observer observer;
    //CompositeDisposable disposable;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_test);
        binding = DataBindingUtil.setContentView(this, R.layout.activity_main_test);

        //disposable = new CompositeDisposable();

        binding.btnObserverCreate.setOnClickListener(v -> {
            testObserverCreate();
        });

        binding.btnObserverFromCallable.setOnClickListener(v ->{
            testObserverFromCallable();
        });

        binding.btnObserverFromFuture.setOnClickListener(v ->{
            testObserverFromFuture();
        });

        binding.btnObserverFromIterable.setOnClickListener(v ->{
            testObserverFromIterable();
        });

        binding.btnObserverFromArray.setOnClickListener(v ->{
            testObserverFromArray();
        });

        binding.btnObserverInterval.setOnClickListener(v ->{
            testObserverInterval();
        });

        binding.btnObserverIntervalSwitchMap.setOnClickListener(v ->{
            testObserverIntervalSwitchMap();
        });

        binding.btnObserverRange.setOnClickListener(v ->{
            testObserverRange();
        });

        binding.btnObserverRepeat.setOnClickListener(v ->{
            testObserverRepeat();
        });

        binding.btnObserverTime.setOnClickListener(v ->{
            testObserverTime();
        });

        binding.btnObserverFilterDebounce.setOnClickListener(v ->{
            testObserverFilterDebounce();
        });

        observer = new Observer() {
            @Override
            public void onSubscribe(Disposable d) {
                binding.content.setText("onSubscribe");
            }

            @Override
            public void onNext(Object o) {
                if(o instanceof TextViewTextChangeEvent){
                    binding.content.setText(binding.content.getText() + ", onNext: " + ((TextViewTextChangeEvent) o).getText().toString());
                }else{
                    binding.content.setText(binding.content.getText() + ", onNext: " + o);
                }

            }

            @Override
            public void onError(Throwable e) {
                binding.content.setText(binding.content.getText() + ", onError: " + e.getMessage());
            }

            @Override
            public void onComplete() {
                binding.content.setText(binding.content.getText() + ", onComplete");
            }
        };
    }

    private void testObserverCreate(){
        final List<String> alphabets = getAlphabetList();

        Observable observable = Observable.create(new ObservableOnSubscribe() {
            @Override
            public void subscribe(ObservableEmitter emitter) {

                try {

                    for (String alphabet : alphabets) {
                        emitter.onNext(alphabet);
                    }

                    emitter.onComplete();

                } catch (Exception e) {

                    emitter.onError(e);
                }
            }
        });

        observable.subscribe(observer);
    }

    private void testObserverFromCallable(){
        Observable observable = Observable.fromCallable(new Callable<String>(){
            @Override
            public String call() throws Exception {
                return getAlphabetList().get(0);
            }
        });

        observable.subscribe(observer);
    }

    private void testObserverFromFuture(){

        ScheduledExecutorService executor = Executors.newSingleThreadScheduledExecutor();
        Future<String> future = executor.schedule(new Callable<String>() {
            @Override
            public String call() throws Exception {
                return getAlphabetList().get(1);
            }
        }, 60, TimeUnit.SECONDS);
        Observable<String> observable = Observable.fromFuture(future);
        observable.observeOn(AndroidSchedulers.mainThread())
                .subscribeOn(Schedulers.newThread())
                .subscribe(observer);
        executor.shutdown();
    }

    private void testObserverFromIterable(){
        Observable observable = Observable.fromIterable(getAlphabetList());
        observable.subscribe(observer);
    }

    private void testObserverFromArray(){
        Observable observable = Observable.fromArray(getAlphabetList());
        observable.subscribe(observer);
    }

    private void testObserverInterval(){
        Observable observable = Observable.interval(2, TimeUnit.SECONDS);
        observable.observeOn(AndroidSchedulers.mainThread()).takeWhile(val -> (long)val < 10l)
                .buffer(2).map(val -> {
                    int result = 0;

                    for(long i : (List<Long>) val){
                        result += i;
                    }

                    return result;
                }).subscribe(observer);
    }

    private void testObserverIntervalSwitchMap(){
        Observable observable = Observable.interval(2, TimeUnit.SECONDS);
        observable.observeOn(AndroidSchedulers.mainThread()).takeWhile(val -> (long)val < 10l)
                .buffer(2).switchMap(val -> {
            return Observable.create(new ObservableOnSubscribe<Long>() {
                @Override
                public void subscribe(ObservableEmitter<Long> emitter) throws InterruptedException {
                    Long result = 0l;

                    for(long i : (List<Long>) val){
                        result += i;
                    }
                    emitter.onNext(result);
                    emitter.onComplete();
                }
            }).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread());
        }).subscribe(observer);
    }

    private void testObserverRange(){
        Observable observable = Observable.range(3, 10);
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void testObserverRepeat(){
        Observable observable = Observable.range(3, 10).repeat(2);
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void testObserverTime(){
        Observable observable = Observable.timer(10, TimeUnit.SECONDS);
        observable.observeOn(AndroidSchedulers.mainThread()).subscribe(observer);
    }

    private void testObserverFilterDebounce(){
        RxTextView.textChangeEvents(binding.testTyping).skipInitialValue()
                .debounce(2, TimeUnit.SECONDS).observeOn(AndroidSchedulers.mainThread()).subscribe(observer);

    }

    private List<String> getAlphabetList(){
        return Arrays.asList(new String[]{
                "a", "b", "c", "d", "e", "f"
        });
    }
}
