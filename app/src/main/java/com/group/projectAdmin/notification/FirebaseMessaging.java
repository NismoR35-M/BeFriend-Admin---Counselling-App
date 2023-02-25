package com.group.projectAdmin.notification;

import android.annotation.SuppressLint;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;

import android.net.Uri;
import android.os.Build;


import androidx.annotation.NonNull;
import androidx.annotation.RequiresApi;
import androidx.core.app.NotificationCompat;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.messaging.FirebaseMessagingService;
import com.google.firebase.messaging.RemoteMessage;
import com.group.projectAdmin.MainActivity;
import com.group.projectAdmin.R;


import java.util.List;


@SuppressWarnings({"ALL", "StatementWithEmptyBody", "UnusedAssignment"})
public class FirebaseMessaging extends FirebaseMessagingService {

    @RequiresApi(api = Build.VERSION_CODES.S)
    @Override
    public void onMessageReceived(@NonNull RemoteMessage remoteMessage) {
        super.onMessageReceived(remoteMessage);

        if(isAppIsInBackground(getApplicationContext())) {

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                sendOAndAboveNotification(remoteMessage);
            } else {
                sendNormalNotification(remoteMessage);
            }

        } else {
            // Don't show notification
        }

    }
    @RequiresApi(api = Build.VERSION_CODES.S)
    private void sendNormalNotification(RemoteMessage remoteMessage){
        String user = ""+remoteMessage.getData().get("sent");
        String icon = ""+remoteMessage.getData().get("icon");
        String title = ""+remoteMessage.getData().get("title");
        String type = ""+remoteMessage.getData().get("type");
        String body = ""+remoteMessage.getData().get("body");
        @SuppressWarnings("unused") RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i = Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = null;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" +  R.raw.notification);
        NotificationCompat.Builder builder = new NotificationCompat.Builder(this)
                .setSmallIcon(Integer.parseInt(icon))
               .setContentText(body)
                .setContentTitle(title)
                .setAutoCancel(true)
                .setSound(defSoundUri)
                .setContentIntent(pIntent);

        NotificationManager notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
        int j = 0;
        if (i>0){
            j=i;
        }
        notificationManager.notify(j,builder.build());
    }

    @RequiresApi(api = Build.VERSION_CODES.O)
    private void sendOAndAboveNotification(RemoteMessage remoteMessage){
        String user = ""+remoteMessage.getData().get("user");
        String icon = ""+remoteMessage.getData().get("icon");
        String title = ""+remoteMessage.getData().get("title");
        String body = ""+remoteMessage.getData().get("body");
        String type = ""+remoteMessage.getData().get("type");
        @SuppressWarnings("unused") RemoteMessage.Notification notification = remoteMessage.getNotification();
        int i= Integer.parseInt(user.replaceAll("[\\D]", ""));
        Intent intent = null;
        intent = new Intent(this, MainActivity.class);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
        PendingIntent pIntent = PendingIntent.getActivity(this, i, intent, PendingIntent.FLAG_ONE_SHOT);
        Uri defSoundUri = Uri.parse("android.resource://" + getApplicationContext().getPackageName() + "/" + R.raw.notification);
        OreoAndAboveNotification notification1 = new OreoAndAboveNotification(this);
        Notification.Builder builder = notification1.getONotifications(title, body, pIntent, defSoundUri, icon);
        int j = 0;
        if (i>0){
            j=i;
        }
        notification1.getManager().notify(j,builder.build());
    }

    @Override
    public void onNewToken(@NonNull String s) {
        super.onNewToken(s);
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        if (user!=null){
            updateToken(s);
        }
    }

    private void updateToken(String tokenRefresh) {
        FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("Tokens");
        Token token = new Token(tokenRefresh);
        ref.child(user.getUid()).setValue(token);
    }
    @SuppressLint("ObsoleteSdkInt")
    private boolean isAppIsInBackground(Context context) {
        boolean isInBackground = true;

        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (Build.VERSION.SDK_INT > Build.VERSION_CODES.KITKAT_WATCH) {
            List<ActivityManager.RunningAppProcessInfo> runningProcesses = am.getRunningAppProcesses();
            for (ActivityManager.RunningAppProcessInfo processInfo : runningProcesses) {
                if (processInfo.importance == ActivityManager.RunningAppProcessInfo.IMPORTANCE_FOREGROUND) {
                    for (String activeProcess : processInfo.pkgList) {
                        if (activeProcess.equals(context.getPackageName())) {
                            isInBackground = false;
                        }
                    }
                }
            }
        }
        else
        {
            List<ActivityManager.RunningTaskInfo> taskInfo = am.getRunningTasks(1);
            ComponentName componentInfo = taskInfo.get(0).topActivity;
            if (componentInfo.getPackageName().equals(context.getPackageName())) {
                isInBackground = false;
            }
        }
        return isInBackground;
    }
}
