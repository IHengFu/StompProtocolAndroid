package ua.naiksoftware.stompclientexample;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import org.java_websocket.WebSocket;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import io.reactivex.FlowableTransformer;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.Disposable;
import io.reactivex.schedulers.Schedulers;
import io.reactivex.subscribers.DisposableSubscriber;
import ua.naiksoftware.stomp.LifecycleEvent;
import ua.naiksoftware.stomp.Stomp;
import ua.naiksoftware.stomp.StompHeader;
import ua.naiksoftware.stomp.client.StompClient;
import ua.naiksoftware.stomp.client.StompMessage;

import static ua.naiksoftware.stompclientexample.RestClient.ANDROID_EMULATOR_LOCALHOST;

public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private SimpleAdapter mAdapter;
    private List<String> mDataSet = new ArrayList<>();
    private StompClient mStompClient;
    private Disposable mRestPingDisposable;
    private final SimpleDateFormat mTimeFormat = new SimpleDateFormat("HH:mm:ss", Locale.getDefault());
    private RecyclerView mRecyclerView;
    private Gson mGson = new GsonBuilder().create();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        mRecyclerView = (RecyclerView) findViewById(R.id.recycler_view);
        mAdapter = new SimpleAdapter(mDataSet);
        mAdapter.setHasStableIds(true);
        mRecyclerView.setAdapter(mAdapter);
        mRecyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, true));
    }

    public void disconnectStomp(View view) {
        if (mStompClient != null)
            mStompClient.disconnect();
    }

    public void connectStomp(View view) {
        mStompClient = Stomp.over(WebSocket.class, "ws://52.169.119.102:8080/telematics-client/v2/websocket");
//                "ws://" + ANDROID_EMULATOR_LOCALHOST
//                + ":" + RestClient.SERVER_PORT + "/telematics-client/v2/websocket");//"/example-endpoint/websocket");

        mStompClient.lifecycle()
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<LifecycleEvent>() {
                    @Override
                    public void onNext(LifecycleEvent lifecycleEvent) {
                        switch (lifecycleEvent.getType()) {
                            case OPENED:
                                toast("Stomp connection opened");
                                break;
                            case ERROR:
                                Log.e(TAG, "Stomp connection error", lifecycleEvent.getException());
                                break;
                            case CLOSED:
                                Log.e(TAG, "Stomp connection closed");
                                break;
                        }
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Stomp lifecycle error", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "Stomp lifecycle complete");
                    }
                });

//                        lifecycleEvent -> {
//
//                });

        // Receive greetings
        mStompClient.topic("/user/topic/serverMessage")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe(new DisposableSubscriber<StompMessage>() {
                    @Override
                    public void onNext(StompMessage topicMessage) {

                        Log.d(TAG, "Received " + topicMessage.getPayload());
                        addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
                    }

                    @Override
                    public void onError(Throwable t) {
                        Log.e(TAG, "Stomp topic error", t);
                    }

                    @Override
                    public void onComplete() {
                        Log.e(TAG, "Stomp connection onComplete");
                    }
                });
//                .subscribe(topicMessage -> {
//                    Log.d(TAG, "Received " + topicMessage.getPayload());
//                    addItem(mGson.fromJson(topicMessage.getPayload(), EchoModel.class));
//                });

        ArrayList<StompHeader> headers = new ArrayList<>();
        headers.add(new StompHeader("userId", "3cbfc7c0-4191-41f0-9e93-bb21888f1c6f"));
        mStompClient.connect(headers);
    }

    public void sendEchoViaStomp(View v) {
        mStompClient.send("/topic/hello-msg-mapping", "Echo STOMP " + mTimeFormat.format(new Date()))
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(TAG, "STOMP echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send STOMP echo", throwable);
                    toast(throwable.getMessage());
                });
    }

    public void sendEchoViaRest(View v) {
        mRestPingDisposable = RestClient.getInstance().getExampleRepository()
                .sendRestEcho("Echo REST " + mTimeFormat.format(new Date()))
                .compose(applySchedulers())
                .subscribe(aVoid -> {
                    Log.d(TAG, "REST echo send successfully");
                }, throwable -> {
                    Log.e(TAG, "Error send REST echo", throwable);
                    toast(throwable.getMessage());
                });
    }

    private void addItem(EchoModel echoModel) {
        mDataSet.add(echoModel.getTitle() + "\n " +echoModel.getContent());
        mAdapter.notifyDataSetChanged();
        mRecyclerView.smoothScrollToPosition(mDataSet.size() - 1);
    }

    private void toast(String text) {
        Log.i(TAG, text);
        Toast.makeText(this, text, Toast.LENGTH_SHORT).show();
    }

    protected <T> FlowableTransformer<T, T> applySchedulers() {
        return tFlowable -> tFlowable
                .unsubscribeOn(Schedulers.newThread())
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread());
    }

    @Override
    protected void onDestroy() {
        mStompClient.disconnect();
        if (mRestPingDisposable != null) mRestPingDisposable.dispose();
        super.onDestroy();
    }
}
