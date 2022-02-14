package com.example.yacovz.leoaccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Rect;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Random;
import java.util.Timer;
import java.util.TimerTask;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by yacovz on 21/03/2018.
 */

public class JAccessibilityService extends AccessibilityService {

    String TAG = "JAccessibilityService";
    public void onAccessibilityEvent(AccessibilityEvent event) {

        Log.v(TAG, String.format(
                "onAccessibilityEvent: [type] %s [class] %s [package] %s [time] %s",
                idToText(event), event.getClassName(), event.getPackageName(), event.getEventTime()));

        if(idToText(event)=="TYPE_VIEW_HOVER_ENTER" || idToText(event)=="TYPE_VIEW_HOVER_EXIT") {
            Log.v(TAG, String.format("onHoverEvent: [scrollX] %s [scrollY] %s",
                    event.getScrollX(), event.getScrollY()));

        }


        final int eventType = event.getEventType();
        String eventText = null;
        switch(eventType) {
            //
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED: case AccessibilityEvent.TYPE_VIEW_CLICKED: case AccessibilityEvent.TYPE_VIEW_FOCUSED: case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                if (eventType == AccessibilityEvent.TYPE_VIEW_CLICKED)
                    eventText = "TYPE_VIEW_CLICKED";
                else if (eventType == AccessibilityEvent.TYPE_VIEW_FOCUSED)
                    eventText = "TYPE_VIEW_FOCUSED";
                else if (eventType == AccessibilityEvent.TYPE_VIEW_SCROLLED)
                    eventText = "TYPE_VIEW_SCROLLED";

                Log.v(TAG, eventText + event.getContentDescription());

                if (event.getPackageName().toString().contains("android.settings"))
                {
                    showFeedback("Indexing started ");
                }


                if (isToCapture(event)) {
                    AccessibilityNodeInfo source = event.getSource();
                    if (source != null) {

                        /*
                        ArrayList<String> list = queryViewTree(source, true);
                        if (list.size() > 0) {
                            String s ="";
                        }



                        */

                        //List<AccessibilityWindowInfo> windows = getWindows();
                        String text ="";
                        try {
                            text = CreateVirtualViewXml(source,eventText,event.getPackageName().toString());
                            Log.v(TAG, text);
                            /*text = "CreateVirtualViewXmlBK: " + CreateVirtualViewXmlBK(source,eventText,event.getPackageName().toString());
                            Log.v(TAG, text);*/
                        }catch(Exception e){
                            Log.e(event.getPackageName().toString(), "Error in CreateVirtualViewXml " + e.toString());
                        }

                        //testParsing();
                        //saveXmlFile(text);


                        try {
                            if (text !=""  ){
                                String groupName = getGroupName();
                                showFeedback("Captured: " + text + " in " + groupName);

                                sendToServer(text,groupName,eventText);


                               // Log.v(TAG, "sendToServer: open waze");
                                //openWazeApp(this,"com.waze");


                            }

                            //addToDb(text,event.getPackageName().toString(),myDb);

                        }catch(Exception e){
                            Log.e(event.getPackageName().toString(), "Error in addToDb " + e.toString());
                        }

                        //VirtualView virtualView = parseViewTree(source, true);
                        // if (virtualView != null) {
                        //String Text = virtualView.ToString();
                        //String Text = virtualView.GetXml();

                            /*
                            Bitmap bitmap = Bitmap.createBitmap(virtualView.boundsInScreenRect.width(), virtualView.boundsInScreenRect.height(), Bitmap.Config.ARGB_8888);
                            Canvas canvas = new Canvas(bitmap);
                            canvas.drawBitmap(bitmap, new Matrix(), null);
                            Paint p = new Paint();
                            p.setColor(Color.WHITE);
                            canvas.drawRect(virtualView.boundsInScreenRect, p);
                            virtualView.DrawBitmap(bitmap);
                            */
                       // if (eventText != null)
                            //showFeedback(eventText.replace("TYPE_VIEW_","") + event.getPackageName().toString());
                        // }



                    }
                }
                break;

        }
    }


    private String groupNamePrefs = "group_name_prefs";
    private String groupNameKey = "group_name_key";

    private String getGroupName ()
    {
        SharedPreferences sp = getSharedPreferences(groupNamePrefs, Activity.MODE_PRIVATE);
        return sp.getString(groupNameKey, "");
    }

    Timer nextScanTimer=null;
    private void startNextScan()
    {
        // minutes to next interval
        int delaytime = 1;

        /*nextScanTimer = new Timer();
        nextScanTimer.schedule(new TimerTask() {
            @Override
            public void run() {
                Log.v(TAG, "startNextScan: open leoaccessibilityservice");
                openApp(getBaseContext(),"com.example.yacovz.leoaccessibilityservice");
                startNextScan();
            }
        }, (3600000/60*delaytime)); // 3600000 an hour
*/
    }

    public static boolean openWazeApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }

        // 32.147339, 34.803869 cinema city glilot

        i.setAction(android.content.Intent.ACTION_VIEW);
        String uri = "waze://?ll=32.141598,34.801754&navigate=yes";
        i.setData(Uri.parse(uri));


        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();
        bundle.putString("key", "key");
        context.startActivity(i,bundle);
        return true;


        /*Intent myIntent = new Intent(String.valueOf(Main2Activity.class));
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
        return true;*/
    }

    public static boolean openApp(Context context, String packageName) {
        PackageManager manager = context.getPackageManager();
        Intent i = manager.getLaunchIntentForPackage(packageName);
        if (i == null) {
            return false;
            //throw new PackageManager.NameNotFoundException();
        }

        /*i.setAction(android.content.Intent.ACTION_VIEW);
        String uri = "waze://?ll=32.141598,34.801754&navigate=yes";
        i.setData(Uri.parse(uri));*/


        i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        Bundle bundle = new Bundle();
        bundle.putString("key", "key");
        context.startActivity(i,bundle);
        return true;


        /*Intent myIntent = new Intent(String.valueOf(Main2Activity.class));
        myIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        context.startActivity(myIntent);
        return true;*/
    }

    private boolean isAppRunning(String appPackageName){
        boolean ret=false;
        final ActivityManager activityManager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        final List<ActivityManager.RunningAppProcessInfo> recentTasks = activityManager.getRunningAppProcesses();

        for (int i = 0; i < recentTasks.size(); i++)
        {
            String pn = recentTasks.get(i).processName;
            if(pn.equalsIgnoreCase(appPackageName)) {
                //Log.d("Executed app", "Application executed : " + recentTasks.get(i).baseActivity.toShortString() + "\t\t ID: " + recentTasks.get(i).id + "");
                ret = true;
                break;
            }
            break;
        }
        return ret;
    }

    /*private void killApp(String packageTokill){

        List<ApplicationInfo> packages;
        PackageManager manager;
        manager = getPackageManager();
        //get a list of installed apps.
        packages = manager.getInstalledApplications(0);

        android.os.Process.killProcess(android.os.Process.myPid());

        ActivityManager mActivityManager = (ActivityManager) MainActivity.this.getSystemService(Context.ACTIVITY_SERVICE);
        String myPackage = getApplicationContext().getPackageName();

        for (ApplicationInfo packageInfo : packages) {

            if((packageInfo.flags & ApplicationInfo.FLAG_SYSTEM)==1) {
                continue;
            }
            if(packageInfo.packageName.equals(myPackage)) {
                continue;
            }
            if(packageInfo.packageName.equals(packageTokill)) {
                mActivityManager.killBackgroundProcesses(packageInfo.packageName);
            }

        }

    }*/

    private String CreateVirtualViewXml(AccessibilityNodeInfo source,String eventType, String eventSource )
    {
        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData = source.getContentDescription().toString();
        }
        Log.v(TAG, "in CreateVirtualViewXml: " +textData);

        String type ="";
        if (source.getClassName() != null )
            type = source.getClassName().toString();

        StringBuilder sb = new StringBuilder();

        //if (type == "android.widget.TextView"){
        if (textData.startsWith("@")){
            sb.append(textData);
        }

        //}

        /*String ViewIdResorceName = source.getViewIdResourceName();
        if (ViewIdResorceName != null && ViewIdResorceName == "com.waze:id/lblEtaTime" ){
            sb.append(textData);
            //return textData;
        }*/

        if(source.getChildCount() >0)
        {
            for (int i=0;i<source.getChildCount();i++)
            {
                sb.append( CreateVirtualViewXml(source.getChild(i),eventType,"") );
            }
        }
        return sb.toString();
    }

    private Pattern pattern;
    private Matcher matcher;
    public boolean validate(final String time){

        final String TIME24HOURS_PATTERN =
                "([01]?[0-9]|2[0-3]):[0-5][0-9]";
        pattern = Pattern.compile(TIME24HOURS_PATTERN);

        matcher = pattern.matcher(time);
        return matcher.matches();

    }

    private String CreateVirtualViewXmlBK(AccessibilityNodeInfo source,String eventType, String eventSource )
    {
        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData = source.getContentDescription().toString();
        }
        Log.v(TAG, textData);

        // extract bounds in parent
        Rect boundsInParent = new Rect(0,0,0,0);
        source.getBoundsInParent(boundsInParent);

        Rect boundsInScreen =  new Rect(0,0,0,0);
        source.getBoundsInScreen(boundsInScreen);

        StringBuilder sb = new StringBuilder();

        sb.append("<View>");

        if (eventType != "")
        {
            sb.append("<Properties>");
            sb.append("<EventType>");
            sb.append(eventType);
            sb.append("</EventType>");
            sb.append("<AppSource>");
            sb.append(eventSource);
            sb.append("</AppSource>");
            sb.append("<DateTime>");
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String datetime = df.format(Calendar.getInstance().getTime());
            sb.append(datetime);
            sb.append("</DateTime>");
            eventType = "";
            sb.append("</Properties>");
        }

        sb.append("<Text>");
        sb.append(textData);
        sb.append("</Text>");

        sb.append("<Type>");
        String type="unknowen";

        if (source.getClassName() != null )
            type = source.getClassName().toString();

        sb.append(type);
        /*if(type.contains("Image"))// == "android.widget.ImageView")
            testActions(source);*/

        sb.append("</Type>");

        sb.append("<ViewIdResorceName>");
        sb.append(source.getViewIdResourceName());
        sb.append("</ViewIdResorceName>");

        sb.append("<boundsInScreenRect>");
        sb.append(boundsInScreen.left + "," + boundsInScreen.top + "," + boundsInScreen.width() + "," + boundsInScreen.height());
        sb.append("</boundsInScreenRect>");

        sb.append("<boundsInParentRect>");
        sb.append(boundsInParent.left + "," + boundsInParent.top + "," + boundsInParent.width() + "," + boundsInParent.height());
        sb.append("</boundsInParentRect>");

        /*
        sb.append("<boundsInWindowInfoRect>");
        AccessibilityWindowInfo windowInfo = source.getWindow();
        if(windowInfo != null)
        {
            Rect boundsInWindowInfo = new Rect(0,0,0,0);
            windowInfo.getBoundsInScreen(boundsInWindowInfo);
            sb.append(boundsInWindowInfo.left + "," + boundsInWindowInfo.top + "," + boundsInWindowInfo.width() + "," + boundsInWindowInfo.height());
        }
        sb.append("</boundsInWindowInfoRect>");
        */
        /*
        sb.append("<CollectionInfo>");
        if(source.getCollectionItemInfo() != null)
        {
            sb.append("<ColumnSpan>" + source.getCollectionItemInfo().getColumnSpan() +  "</ColumnSpan>");
            sb.append("<ColumnIndex>" + source.getCollectionItemInfo().getColumnIndex() +  "</ColumnIndex>");
            sb.append("<RowSpan>" + source.getCollectionItemInfo().getRowSpan() +  "</RowSpan>");
            sb.append("<RowIndex>" + source.getCollectionItemInfo().getRowIndex() +  "</RowIndex>");
            sb.append("<isHeading>" + source.getCollectionItemInfo().isHeading() +  "</isHeading>");
            sb.append("<isSelected>" + source.getCollectionItemInfo().isSelected() +  "</isSelected>");
        }
        sb.append("</CollectionInfo>");
        */


        sb.append("<ChildViews>");
        if(source.getChildCount() >0)
        {
            for (int i=0;i<source.getChildCount();i++)
            {
                sb.append( CreateVirtualViewXmlBK(source.getChild(i),eventType,"") );
            }
        }
        sb.append("</ChildViews>");

        sb.append("</View>");

        return sb.toString();
    }

    private void sendToServer(String text,String group_name,String eventType)
    {

        AsyncInvokeURLTask task = null;
        try {
            task = new AsyncInvokeURLTask(
                    text,group_name,eventType,getApplicationContext()
                    , new AsyncInvokeURLTask.OnPostExecuteListener() {
                @Override
                public void onPostExecute(String result) {
                    // TODO Auto-generated method stub
                    String f= "";
                    //Log.v(TAG, "in onPostExecute: " +result);
                    showFeedback("Server Response: " + result);
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
            Log.v(TAG, "in Exception: " + e.toString());
        }
        task.execute();
    }

    @Override
    public void onInterrupt() {}

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v("JAccessibilityService", "AccessibilityService allowed");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        //info.flags = AccessibilityServiceInfo.DEFAULT ;
        info.flags =  AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_BRAILLE;
        setServiceInfo(info);

        startNextScan();
    }

    private String idToText(AccessibilityEvent event) {
        switch (event.getEventType()) {
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_START:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_START";
            case AccessibilityEvent.TYPE_TOUCH_EXPLORATION_GESTURE_END:
                return "TYPE_TOUCH_EXPLORATION_GESTURE_END";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_START:
                return "TYPE_TOUCH_INTERACTION_START";
            case AccessibilityEvent.TYPE_TOUCH_INTERACTION_END:
                return "TYPE_TOUCH_INTERACTION_END";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_START:
                return "TYPE_GESTURE_DETECTION_START";
            case AccessibilityEvent.TYPE_GESTURE_DETECTION_END:
                return "TYPE_GESTURE_DETECTION_END";
            case AccessibilityEvent.TYPE_VIEW_HOVER_ENTER:
                return "TYPE_VIEW_HOVER_ENTER";
            case AccessibilityEvent.TYPE_VIEW_HOVER_EXIT:
                return "TYPE_VIEW_HOVER_EXIT";
            case AccessibilityEvent.TYPE_VIEW_SCROLLED:
                return "TYPE_VIEW_SCROLLED";
            case AccessibilityEvent.TYPE_VIEW_CLICKED:
                return "TYPE_VIEW_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_LONG_CLICKED:
                return "TYPE_VIEW_LONG_CLICKED";
            case AccessibilityEvent.TYPE_VIEW_FOCUSED:
                return "TYPE_VIEW_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_SELECTED:
                return "TYPE_VIEW_SELECTED";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUSED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUSED";
            case AccessibilityEvent.TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED:
                return "TYPE_VIEW_ACCESSIBILITY_FOCUS_CLEARED";
            case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                return "TYPE_WINDOW_STATE_CHANGED";
            case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                return "TYPE_NOTIFICATION_STATE_CHANGED";
            case AccessibilityEvent.TYPE_ANNOUNCEMENT:
                return "TYPE_ANNOUNCEMENT";
            case AccessibilityEvent.TYPE_WINDOWS_CHANGED:
                return "TYPE_WINDOWS_CHANGED";
            case AccessibilityEvent.TYPE_WINDOW_CONTENT_CHANGED:
                return "TYPE_WINDOW_CONTENT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_CHANGED:
                return "TYPE_VIEW_TEXT_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_SELECTION_CHANGED:
                return "TYPE_VIEW_TEXT_SELECTION_CHANGED";
            case AccessibilityEvent.TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY:
                return "TYPE_VIEW_TEXT_TRAVERSED_AT_MOVEMENT_GRANULARITY";
        }
        return "Unknown";
    }

    static String[] packages=null;
    private boolean isToCapture(AccessibilityEvent event)
    {
        boolean ret = false;

        if (packages == null) {
            Resources res = getResources();
            packages = res.getStringArray(R.array.allowed_packages);
        }

        String pn = event.getPackageName().toString();

        for (int i=0;i< packages.length;i++  )
        {
            if(pn.contains(packages[i]))
            {
                ret = true;
                break;
            }
        }

        return ret;
    }

    private void showFeedback(String message)
    {
        final Toast toast = Toast.makeText(getApplicationContext(), message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 2000);

    }
}
