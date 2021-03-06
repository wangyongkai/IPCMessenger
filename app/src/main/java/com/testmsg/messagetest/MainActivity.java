package com.testmsg.messagetest;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import static com.testmsg.messagetest.MessengerService.MSG_SAY_HELLO;

public class MainActivity extends AppCompatActivity {


    //如何理解aidl 牢记三个类  Stub类  Proxy类  AIDL接口 三个作用


    //Messenger类只是一个封装类
    //1.IMessenger相当于定义的AIDL接口


    //Stub类
    //Handler中getIMessenger()返回一个MessengerImpl
//    private final class MessengerImpl extends IMessenger.Stub { //持有外部类 Handler
//        public void send(Message msg) {//这个方法就是到服务端后 调用的方法
//            msg.sendingUid = Binder.getCallingUid();
//            Handler.this.sendMessage(msg);
//        }
//    }


    //构建代理类
//    public Messenger(IBinder target) {
//        mTarget = IMessenger.Stub.asInterface(target);
//    }


    /**
     * 用于Handler里的消息类型
     */
    static final int MSG_SAY_HELLO_ACTIVITY = 2;

    /**
     * 在Service处理Activity传过来消息的Handler
     */
    class IncomingActivityHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            switch (msg.what) {
                case MSG_SAY_HELLO_ACTIVITY:
                    Toast.makeText(getApplicationContext(), "hello this is from service!", Toast.LENGTH_SHORT).show();

                    break;
                default:
                    super.handleMessage(msg);
            }
        }
    }

    final Messenger aMessenger = new Messenger(new IncomingActivityHandler());

    /**
     * 向Service发送Message的Messenger对象
     */
    Messenger mService = null;

    /**
     * 判断有没有绑定Service
     */
    boolean mBound;

    private ServiceConnection mConnection = new ServiceConnection() {
        public void onServiceConnected(ComponentName className, IBinder service) {
            // Activity已经绑定了Service
            // 通过参数service来创建Messenger对象，这个对象可以向Service发送Message，与Service进行通信
            mService = new Messenger(service);//拿到的是IMessenger接口中的代理对象
            mBound = true;
        }

        public void onServiceDisconnected(ComponentName className) {
            mService = null;
            mBound = false;
        }
    };


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ((TextView) findViewById(R.id.text)).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sayHello(view);
            }
        });
    }


    @Override
    protected void onStart() {
        super.onStart();
        // 绑定Service
        Intent intent = new Intent(this, MessengerService.class);
        intent.putExtra("messenger", aMessenger);
        bindService(intent, mConnection,
                Context.BIND_AUTO_CREATE);
    }

    @Override
    protected void onStop() {
        super.onStop();
        // 解绑
        if (mBound) {
            unbindService(mConnection);
            mBound = false;
        }
    }


    public void sayHello(View v) {
        if (!mBound) return;
        // 向Service发送一个Message
        Message msg = Message.obtain(null, MSG_SAY_HELLO, 0, 0);
        try {
            mService.send(msg);
        } catch (RemoteException e) {
            e.printStackTrace();
        }
    }
}
