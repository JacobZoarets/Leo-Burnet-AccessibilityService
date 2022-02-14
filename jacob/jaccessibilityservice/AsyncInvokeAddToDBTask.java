package com.meos.jacob.jaccessibilityservice;

import android.os.AsyncTask;
import android.util.Log;

import org.json.JSONException;
import org.json.JSONObject;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * Created by owner on 13/01/2016.
 */
public class AsyncInvokeAddToDBTask  extends AsyncTask<Void, Void, String> {

    private OnPostExecuteListener mPostExecuteListener = null;
    String text;
    String sourceApp;
    DBAdapter myDb;

    public static interface OnPostExecuteListener{
        void onPostExecute(String result);
    }

    AsyncInvokeAddToDBTask(
            String _text,
            String _sourceApp,
            DBAdapter myDb,
            OnPostExecuteListener postExecuteListener) throws Exception {

        text = _text;
        sourceApp = _sourceApp;
        this.myDb = myDb;

        mPostExecuteListener = postExecuteListener;
        if (mPostExecuteListener == null)
            throw new Exception("Param cannot be null.");
    }

    // XML node keys
    static final String KEY_ITEM = "View"; // parent node

    static final String KEY_TEXT = "Text";
    static final String KEY_TYPE = "Type";
    static final String KEY_RESORCE_NAME = "ViewIdResorceName";

    @Override
    protected String doInBackground(Void... params) {

        XMLParser parser = new XMLParser();
        Document doc = parser.getDomElement(text); // getting DOM element
        if (doc != null) {
            NodeList nl = doc.getElementsByTagName(KEY_ITEM);

            // looping through all item nodes <item>
            for (int i = 0; i < nl.getLength(); i++) {

                Element e = (Element) nl.item(i);
                String text = parser.getValue(e, KEY_TEXT); // text child value
                String type = parser.getValue(e, KEY_TYPE); // type child value
                String ViewIdResorceName = parser.getValue(e, KEY_RESORCE_NAME); // ResorceName child value
                //String dateTime = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss").format(Calendar.getInstance().getTime());
                //long timeInMilis = System.currentTimeMillis();//Calendar.getInstance().getTimeInMillis();


                // is to add to db
                if (isValidTextNode(text, type, ViewIdResorceName)) {
                    long newId = myDb.insertRow(text, type, ViewIdResorceName, "");
                    Log.w("DBLogger", "newId=" + newId + "  text=" + text);
                }

            }
        }
        return String.valueOf("1");
    }

    public static final String[] unwantedWords = new String[] {"likers_text", "commenters_text", "like_container", "comment_container", "share_container"};
    private boolean isValidTextNode(String text, String nodeType,String ViewIdResorceName)
    {
        boolean retVal;

        if(text.trim() != "") {
            retVal = true;
            if (nodeType == "android.widget.TextView" || nodeType == "android.view.View") {

                for (int i = 0; i < unwantedWords.length; i++) {
                    if (ViewIdResorceName.contains(unwantedWords[i])) {
                        retVal = false;
                        break;
                    }
                }
            }
        }else
        {
            retVal = false;
        }

        return retVal;
    }

    @Override
    protected void onPostExecute(String result) {
        if (mPostExecuteListener != null){
            try {
                JSONObject json = new JSONObject(result);
                mPostExecuteListener.onPostExecute(json.toString());
            } catch (JSONException e){
                e.printStackTrace();
            }
        }
    }
}
