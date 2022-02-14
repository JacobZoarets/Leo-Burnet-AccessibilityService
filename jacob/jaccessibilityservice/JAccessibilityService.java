package com.meos.jacob.jaccessibilityservice;

import android.accessibilityservice.AccessibilityService;
import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.res.Resources;
import android.graphics.Rect;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.accessibility.AccessibilityEvent;
import android.view.accessibility.AccessibilityNodeInfo;
import android.view.accessibility.AccessibilityWindowInfo;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Queue;


/**
 * Accessibility Service test
 */
public class JAccessibilityService extends AccessibilityService {

    DBAdapter myDb;

    @Override
    public void onCreate() {
        //getServiceInfo().flags = AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE;
        Log.v(TAG, "Service Created");
        openDB();
    }
    @Override
    public void onDestroy() {
        super.onDestroy();
        closeDB();
    }


    private void openDB() {
        myDb = new DBAdapter(this);
        myDb.open();
    }
    private void closeDB() {
        myDb.close();
    }


    @Override
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
            case AccessibilityEvent.TYPE_VIEW_CLICKED: case AccessibilityEvent.TYPE_VIEW_FOCUSED: case AccessibilityEvent.TYPE_VIEW_SCROLLED:

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
                        }catch(Exception e){
                            Log.e(event.getPackageName().toString(), "Error in CreateVirtualViewXml " + e.toString());
                        }

                        //testParsing();
                        //saveXmlFile(text);


                        try {
                        //sendToServer(text,event.getPackageName().toString(),eventText);
                            addToDb(text,event.getPackageName().toString(),myDb);
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

                            showFeedback(eventText.replace("TYPE_VIEW_","") + event.getPackageName().toString());
                       // }



                    }
                }
                break;

        }
    }

    private void sendToServer(String text,String appSource,String eventType)
    {

        AsyncInvokeURLTask task = null;
        try {
            task = new AsyncInvokeURLTask(
                    text,appSource,eventType,getApplicationContext()
                    , new AsyncInvokeURLTask.OnPostExecuteListener() {
                @Override
                public void onPostExecute(String result) {
                    // TODO Auto-generated method stub
                    String f= "";
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        task.execute();
    }


    private void addToDb(String text,String appSource,DBAdapter myDb)
    {
        AsyncInvokeAddToDBTask task = null;
        try {
            task = new AsyncInvokeAddToDBTask(
                    text,appSource,myDb , new AsyncInvokeAddToDBTask.OnPostExecuteListener() {
                @Override
                public void onPostExecute(String result) {
                    // TODO Auto-generated method stub
                    String f= result;
                }

            });
        } catch (Exception e) {
            e.printStackTrace();
        }
        task.execute();
    }


    private void testActions(AccessibilityNodeInfo source)
    {
        Bundle arguments = new Bundle();
        arguments.putInt(AccessibilityNodeInfo.ACTION_ARGUMENT_MOVEMENT_GRANULARITY_INT,
                AccessibilityNodeInfo.MOVEMENT_GRANULARITY_CHARACTER);
        arguments.putBoolean(AccessibilityNodeInfo.ACTION_ARGUMENT_EXTEND_SELECTION_BOOLEAN,
                true);
        source.performAction(AccessibilityNodeInfo.ACTION_SELECT, null);
        source.performAction(AccessibilityNodeInfo.ACTION_COPY,null);

    }


    private void testParsing()
    {
        ArrayList<AccessibilityNodeInfo> mNodes = getNodesFromWindows();

        ArrayList<AccessibilityNodeInfo> theseleafs =
                new ArrayList<AccessibilityNodeInfo>();
        AccessibilityNodeInfo thisnode;
        Queue<AccessibilityNodeInfo> q = new LinkedList<AccessibilityNodeInfo>();
        for (AccessibilityNodeInfo n : mNodes) {
            q.add(n);
        }
        while (!q.isEmpty()) {
            thisnode = q.poll();
            if (shouldIncludeNode(thisnode)) {
                //Add only if it fulfills all requirements!
                theseleafs.add(thisnode);
            }
            for (int i=0; i<thisnode.getChildCount(); ++i) {
                AccessibilityNodeInfo n = thisnode.getChild(i);
                if (n != null) q.add(n); // Add only if not null!
            }
        };
        //Log("MyTag", theseleafs.size() + " leafs in this node!");
    }

    private boolean shouldIncludeNode(AccessibilityNodeInfo node)
    {
        return true;
    }
    private ArrayList<AccessibilityNodeInfo> getNodesFromWindows() {
        List<AccessibilityWindowInfo> windows = getWindows();
        ArrayList<AccessibilityNodeInfo> nodes =
                new ArrayList<AccessibilityNodeInfo>();
        if (windows.size() > 0) {
            for (AccessibilityWindowInfo window : windows) {
                nodes.add(window.getRoot());
            }
        }
        return nodes;
    }

    private HashSet nodeIDs = new HashSet();
    private ArrayList<String> queryViewTree(AccessibilityNodeInfo source ,boolean isToFollowParent)
    {
        ArrayList<String> list = new ArrayList<String>();

        if (isToFollowParent)
        {
            if(source.getParent() != null && source.getPackageName() == source.getParent().getPackageName()){
                list.addAll(queryViewTree(source.getParent(), isToFollowParent));
            }
            if(isToFollowParent == false)
                return list;

            isToFollowParent = false;
        }


        if(source.getChildCount() > 0)
        {
            for (int i=0;i< source.getChildCount(); i++  )
            {

                if (source.getChild(i).getChildCount() > 0) {
                    list.addAll(queryViewTree(source.getChild(i), isToFollowParent));
                }



                if (source.getChild(i).getText() != null) {
                    list.add(source.getChild(i).getText().toString());
                } else if (source.getChild(i).getContentDescription() != null) {
                    list.add(source.getChild(i).getContentDescription().toString());
                }
                //list.add("----------------");
            }


        }
       /* else {
            if(source.getText() != null) {
                list.add(source.getText().toString());
            }else if(source.getContentDescription() != null)
            {
                list.add(source.getContentDescription().toString());
            }
        }*/

        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData =source.getContentDescription().toString();
        }
        //Log.v("WindowIDTag" , String.valueOf(source.getWindowId()) );

        Log.v(TAG ,String.valueOf(source.getWindowId())  + " - " + source.getViewIdResourceName() + " - " +  String.valueOf(source.getClassName())  + " - " + textData);

        if(source.getViewIdResourceName() != null)
        {
            //RelativeLayout rl = source.getClass().getResourceAsStream()asSubclass(new RelativeLayout(null) );
        }

        if(list.size()>0)
        {
            String d="";
        }

        return list;
    }

    private VirtualView CreateVirtualView(AccessibilityNodeInfo source)
    {
        // extract text
        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData =source.getContentDescription().toString();
        }

        // extract bounds in parent
        Rect boundsInParent = new Rect(0,0,0,0);
        source.getBoundsInParent(boundsInParent);

        Rect boundsInScreen =  new Rect(0,0,0,0);
        source.getBoundsInScreen(boundsInScreen);
        VirtualView virtualView = new VirtualView(source.getClassName().toString(),source.getViewIdResourceName(),textData,boundsInParent,boundsInScreen,null);

        return virtualView;
    }

    private VirtualView parseViewTree(AccessibilityNodeInfo source ,boolean isToFollowParent)
    {
        VirtualView virtualView = null;

        if (isToFollowParent)
        {
            if(source.getParent() != null && source.getPackageName() == source.getParent().getPackageName()){
                virtualView = parseViewTree(source.getParent(), isToFollowParent);
            }

            if(isToFollowParent == false) {
                return virtualView;
            }

            isToFollowParent = false;
        }


        virtualView = CreateVirtualView(source);

        if(source.getChildCount() > 0)
        {
            for (int i=0;i< source.getChildCount(); i++  )
            {
                VirtualView childVirtualView = CreateVirtualView(source.getChild(i));

                if (source.getChild(i).getChildCount() > 0) {
                    VirtualView child = parseViewTree(source.getChild(i), isToFollowParent);
                    childVirtualView.addChild(child);
               }

                virtualView.addChild(childVirtualView);
            }
        }


        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData =source.getContentDescription().toString();
        }

        Log.v(TAG, String.valueOf(source.getWindowId()) + " - " + source.getViewIdResourceName() + " - " + String.valueOf(source.getClassName()) + " - " + textData);

        if(virtualView != null)
        {
            String d="";
        }

        return virtualView;
    }


    private String CreateVirtualViewXml(AccessibilityNodeInfo source,String eventType, String eventSource )
    {
        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData = source.getContentDescription().toString();
        }

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
        if(type.contains("Image"))// == "android.widget.ImageView")
            testActions(source);

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
                sb.append( CreateVirtualViewXml(source.getChild(i),eventType,"") );
            }
        }
        sb.append("</ChildViews>");

        sb.append("</View>");

        return sb.toString();
    }

    private VirtualView parseViewTreeXML(AccessibilityNodeInfo source ,boolean isToFollowParent)
    {
        VirtualView virtualView = null;

        if (isToFollowParent)
        {
            if(source.getParent() != null && source.getPackageName() == source.getParent().getPackageName()){
                virtualView = parseViewTree(source.getParent(), isToFollowParent);
            }

            if(isToFollowParent == false) {
                return virtualView;
            }

            isToFollowParent = false;
        }


        virtualView = CreateVirtualView(source);

        if(source.getChildCount() > 0)
        {
            for (int i=0;i< source.getChildCount(); i++  )
            {
                VirtualView childVirtualView = CreateVirtualView(source.getChild(i));

                if (source.getChild(i).getChildCount() > 0) {
                    VirtualView child = parseViewTree(source.getChild(i), isToFollowParent);
                    childVirtualView.addChild(child);
                }

                virtualView.addChild(childVirtualView);
            }
        }


        String textData = "";
        if(source.getText() != null) {
            textData = source.getText().toString();
        }else if(source.getContentDescription() != null)
        {
            textData =source.getContentDescription().toString();
        }

        Log.v(TAG ,String.valueOf(source.getWindowId())  + " - " + source.getViewIdResourceName() + " - " +  String.valueOf(source.getClassName())  + " - " + textData);

        if(virtualView != null)
        {
            String d="";
        }

        return virtualView;
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
        }, 500);

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






    static final String TAG = "JacobRecorderService";

    @Override
    public void onInterrupt() {
        Log.v(TAG, "INTERRUPTED");
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();
        Log.v(TAG, "AccessibilityService allowed");
        AccessibilityServiceInfo info = new AccessibilityServiceInfo();
        //info.flags = AccessibilityServiceInfo.DEFAULT ;
        info.flags =  AccessibilityServiceInfo.DEFAULT | AccessibilityServiceInfo.FLAG_REQUEST_TOUCH_EXPLORATION_MODE | AccessibilityServiceInfo.FLAG_REPORT_VIEW_IDS;
        info.eventTypes = AccessibilityEvent.TYPES_ALL_MASK;
        info.feedbackType = AccessibilityServiceInfo.FEEDBACK_BRAILLE;
        setServiceInfo(info);

    }

    /**
     * Converts an ID returned by AccessibilityEvent.getEventType()
     * to a representative String
     */
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


    /**
     * Converts gestureID to a representative String
     * @param gID
     * @return
     */
    private String gIdToString(int gID) {
        switch(gID) {
            case 1: return "GESTURE_SWIPE_UP";
            case 2: return "GESTURE_SWIPE_DOWN";
            case 3: return "GESTURE_SWIPE_LEFT";
            case 4: return "GESTURE_SWIPE_RIGHT";
            case 5: return "GESTURE_SWIPE_LEFT_AND_RIGHT";
            case 6: return "GESTURE_SWIPE_RIGHT_AND_LEFT";
            case 7: return "GESTURE_SWIPE_UP_AND_DOWN";
            case 8: return "GESTURE_SWIPE_DOWN_AND_UP";
            case 9: return "GESTURE_SWIPE_LEFT_AND_UP";
            case 10: return "GESTURE_SWIPE_LEFT_AND_DOWN";
            case 11: return "GESTURE_SWIPE_RIGHT_AND_UP";
            case 12: return "GESTURE_SWIPE_RIGHT_AND_DOWN";
            case 13: return "GESTURE_SWIPE_UP_AND_LEFT";
            case 14: return "GESTURE_SWIPE_UP_AND_RIGHT";
            case 15: return "GESTURE_SWIPE_DOWN_AND_LEFT";
            case 16: return "GESTURE_SWIPE_DOWN_AND_RIGHT";
        }
        return "UNKNOWN";
    }

}