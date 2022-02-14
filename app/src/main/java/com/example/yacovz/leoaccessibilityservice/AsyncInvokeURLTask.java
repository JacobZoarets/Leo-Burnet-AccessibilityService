package com.example.yacovz.leoaccessibilityservice;

import android.content.Context;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Environment;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.net.URLEncoder;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class AsyncInvokeURLTask extends AsyncTask<Void, Void, String> {

    String text;
    String sourceApp;
    String eventType;
    Context context;
    private OnPostExecuteListener mPostExecuteListener = null;

    public static interface OnPostExecuteListener{
        void onPostExecute(String result);
    }

    AsyncInvokeURLTask(
            String _text,
            String _sourceApp, String _eventType,Context _context,
            OnPostExecuteListener postExecuteListener) throws Exception {

        text = _text;
        context = _context;
        sourceApp = _sourceApp;
        eventType = _eventType;
        mPostExecuteListener = postExecuteListener;
        if (mPostExecuteListener == null)
            throw new Exception("Param cannot be null.");
    }

    String TAG = "JAccessibilityService";

    @Override
    protected String doInBackground(Void... params) {

        String result = "";


        String user_name = Uri.encode(text);
        String group_name = Uri.encode(sourceApp);

        String url = "http://lbi.co.il/projects/MetroLiveTrafficSign/GroupsManager.aspx?user_name="+user_name+"&group_name=" + group_name;

        // String url = "http://lbi.co.il/projects/MetroLiveTrafficSign/MotorcycleTraffic.aspx?waze_now_time=" + waze_now_time + "&waze_arrival_time=" + text+ "&type=car" ;

        //result = String.valueOf(request(url));
        try {
            result = sendGet(url);

        } catch (Exception e) {
            e.printStackTrace();
            Log.e(TAG, "error: " + e.toString());
            result ="error in sendGet()";
        }
        Log.v(TAG, "Server Response: " + result);
        //showFeedback(result);
//perform request
        //httpClient.execute(httpGet)

        /*try{
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String datea = df.format(Calendar.getInstance().getTime());
            String fileName = datea + ".xml";
            uploadAsFile( fileName ,text);

        }catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }*/

       // java.net.URISyntaxException: Illegal character in query at index 103:
        //http://meos1000.com/doIndex.aspx?text=ynet_p_ynet_p_&sourceApp=com.android.systemui&dateTime=2015-11-15 19:00:03.615+0200
        /*
        // Create a new HttpClient and Post Header
        HttpClient httpclient = new DefaultHttpClient();
        HttpPost httppost = new HttpPost(mNoteItWebUrl);

        try {
            // Add parameters
            httppost.setEntity(new UrlEncodedFormEntity(mParams));

            // Execute HTTP Post Request
            HttpResponse response = httpclient.execute(httppost);
            HttpEntity entity = response.getEntity();
            if (entity != null){
                InputStream inStream = entity.getContent();
                result = convertStreamToString(inStream);
            }
        } catch (ClientProtocolException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
        */
        return result;
    }

    // HTTP GET request
    private String sendGet(String url) throws Exception {

        //String url = "http://www.google.com/search?q=mkyong";

        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", "USER_AGENT");

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();

    }

    private void showFeedback(String message)
    {
        /*activity.runOnUiThread(new Runnable() {
            public void run() {
                Toast.makeText(activity, "Hello", Toast.LENGTH_SHORT).show();
            }
        });*/

        final Toast toast = Toast.makeText(context, message, Toast.LENGTH_SHORT);
        toast.show();

        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                toast.cancel();
            }
        }, 500);

    }

    private void saveXmlFile(String textXML)
    {
        try{
            DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
            String datea = df.format(Calendar.getInstance().getTime());

            String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/SearchFiles/" + datea + ".xml";;

            BufferedWriter bufferedWriter=null;
            // open file to read first line
            try {
                bufferedWriter = new BufferedWriter(new FileWriter(filePath));
                bufferedWriter.write(textXML);
                bufferedWriter.close();
            }

            catch (IOException e) {
                e.printStackTrace();
            }
            finally{
                bufferedWriter.close();
            }

        }catch(Exception e){
            Log.e("log_tag", "Error in http connection " + e.toString());
        }

    }
/*
    private String GetTextFile()
    {
        StringBuilder sb = new StringBuilder();

        // write the application source
        sb.append(sourceApp);
        sb.append("\n");

        // write the event type
        sb.append(eventType);
        sb.append("\n");

        // write the event time
        DateFormat df = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
        String datea = df.format(Calendar.getInstance().getTime());
        sb.append(datea);
        sb.append("\n");

        // write the text
        for (int i=0;i<mParams.size();i++   )
        {
            String line = mParams.get(i).trim();

            if(line != "")
            {
                sb.append(line);
                sb.append("\n");
            }
        }

        return sb.toString();

    }
*/

    private String getStoreDirectory()
    {
        String STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/SearchFiles/";
        File storeDirectory = new File(STORE_DIRECTORY);
        if(!storeDirectory.exists()) {
            boolean success = storeDirectory.mkdirs();
            if(!success) {
                Log.e("", "failed to create file storage directory.");
                return "";
            }
        }
        return STORE_DIRECTORY;
    }

    private String getTagsStoreDirectory()
    {
        String STORE_DIRECTORY = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/SearchFilesForTags/";
        File storeDirectory = new File(STORE_DIRECTORY);
        if(!storeDirectory.exists()) {
            boolean success = storeDirectory.mkdirs();
            if(!success) {
                Log.e("", "failed to create file storage directory.");
                return "";
            }
        }
        return STORE_DIRECTORY;
    }

    int serverResponseCode =0;
    public int uploadAsFile(String fileName,String sourceData) {

        String upLoadServerUri = "http://meos1000.com/XmlUploader.aspx";
        // String upLoadServerUri ="http://localhost:13680/MeSearch/Uploader.aspx";
        //String fileName = sourceFileUri;

        HttpURLConnection conn = null;
        DataOutputStream dos = null;
        String lineEnd = "\r\n";
        String twoHyphens = "--";
        String boundary = "*****";
        int bytesRead, bytesAvailable, bufferSize;
        byte[] buffer;
        int maxBufferSize = 1 * 1024 * 1024;
        //File sourceFile = new File(sourceFileUri);
        FileOutputStream fos=null;

            try {

                String filePath = Environment.getExternalStorageDirectory().getAbsolutePath()+ "/SearchFiles/" + fileName;;

                BufferedWriter bufferedWriter;
                // open file to read first line
                try {
                    bufferedWriter = new BufferedWriter(new FileWriter(filePath));
                    bufferedWriter.write(sourceData);
                    bufferedWriter.close();
                }

                catch (IOException e) {
                    e.printStackTrace();
                }
                finally{
                    // br.close();
                }


                // open a URL connection to the Servlet
                FileInputStream fileInputStream = new FileInputStream(filePath);

                //FileInputStream fileOut= new FileInputStream();
                URL url = new URL(upLoadServerUri);

                // Open a HTTP  connection to  the URL
                conn = (HttpURLConnection) url.openConnection();
                conn.setDoInput(true); // Allow Inputs
                conn.setDoOutput(true); // Allow Outputs
                conn.setUseCaches(false); // Don't use a Cached Copy
                conn.setRequestMethod("POST");
                conn.setRequestProperty("Connection", "Keep-Alive");
                conn.setRequestProperty("ENCTYPE", "multipart/form-data");
                conn.setRequestProperty("Content-Type", "multipart/form-data;boundary=" + boundary);
                conn.setRequestProperty("uploaded_file", fileName);

                dos = new DataOutputStream(conn.getOutputStream());

                dos.writeBytes(twoHyphens + boundary + lineEnd);
                String uploaded_file= "";

                //String str1="\"Rajesh\"";
                String strName="name=" + "\"uploaded_file" + "\"" + ";";
                String strFilename = "filename=" + "\"" + fileName + "\"";

                dos.writeBytes("Content-Disposition: form-data; " + strName + strFilename + lineEnd);

                dos.writeBytes(lineEnd);

                // create a buffer of  maximum size
                bytesAvailable =  fileInputStream.available();

                bufferSize = Math.min(bytesAvailable, maxBufferSize);
                buffer = new byte[bufferSize];

                // read file and write it into form...
                bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                while (bytesRead > 0) {

                    dos.write(buffer, 0, bufferSize);
                    bytesAvailable = fileInputStream.available();
                    bufferSize = Math.min(bytesAvailable, maxBufferSize);
                    bytesRead = fileInputStream.read(buffer, 0, bufferSize);

                }

                // send multipart form data necesssary after file data...
                dos.writeBytes(lineEnd);
                dos.writeBytes(twoHyphens + boundary + twoHyphens + lineEnd);

                // Responses from the server (code and message)
                serverResponseCode = conn.getResponseCode();
                String serverResponseMessage = conn.getResponseMessage();

                Log.i("uploadFile", "HTTP Response is : "
                        + serverResponseMessage + ": " + serverResponseCode);


                //close the streams //
                fileInputStream.close();
                dos.flush();
                dos.close();


                File file = new File(filePath);

                if (file.exists()) {
                    if(eventType == "TYPE_VIEW_FOCUSED" || eventType == "TYPE_VIEW_CLICKED") {
                        filePath =  getTagsStoreDirectory() + fileName;
                        File newfile = new File(filePath);
                        file.renameTo(newfile);
                    }
                    file.delete();
                }



            } catch (MalformedURLException ex) {

                //dialog.dismiss();
                ex.printStackTrace();

                Log.e("Upload file to server", "error: " + ex.getMessage(), ex);
            } catch (Exception e) {

                // dialog.dismiss();
                e.printStackTrace();

            }
            //dialog.dismiss();
            return serverResponseCode;

         // End else block
    }


    @Override
    protected void onPostExecute(String result) {
        if (mPostExecuteListener != null){
            mPostExecuteListener.onPostExecute(result);
        }
    }

    private static String convertStreamToString(InputStream is){
        BufferedReader reader = new BufferedReader(
                new InputStreamReader(is));
        StringBuilder sb = new StringBuilder();

        String line = null;

        try {
            while ((line = reader.readLine()) != null) {
                sb.append(line + "\n");
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return sb.toString();
    }
} // AsyncInvokeURLTask