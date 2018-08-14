package com.testmsg.messagetest;

import android.app.Service;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.widget.Toast;

public class MessengerService extends Service {
    /**
     * 用于Handler里的消息类型
     */
    static final int MSG_SAY_HELLO = 1;
    Messenger aMessenger;

    /**
     * 在Service处理Activity传过来消息的Handler
     */
    class IncomingHandler extends Handler {

        @Override
        public void handleMessage(Message msg) {
            Message msgtoactivity = Message.obtain(null, 2, 0, 0);
            switch (msg.what) {
                case MSG_SAY_HELLO:
                    Toast.makeText(getApplicationContext(), "hello this is from activity!", Toast.LENGTH_SHORT).show();
                    aMessenger = msg.replyTo;//此种方式获取客户端的Messenger 而不是用之前的aMessenger = intent.getParcelableExtra("messenger");
                    try {
                        aMessenger.send(msgtoactivity);
                    } catch (RemoteException e) {
                        e.printStackTrace();
                    }
                    break;
                default:
                    super.handleMessage(msgtoactivity);
            }
        }
    }


    /**
     * 这个Messenger可以关联到Service里的Handler，Activity用这个对象发送Message给Service，Service通过Handler进行处理。
     */
    final Messenger mMessenger = new Messenger(new IncomingHandler());

    /**
     * 当Activity绑定Service的时候，通过这个方法返回一个IBinder，Activity用这个IBinder创建出的Messenger，就可以与Service的Handler进行通信了
     */
    @Override
    public IBinder onBind(Intent intent) {
        Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT).show();
        aMessenger = intent.getParcelableExtra("messenger");
        return mMessenger.getBinder();
    }
}
