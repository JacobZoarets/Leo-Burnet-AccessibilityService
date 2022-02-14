package com.meos.jacob.jaccessibilityservice;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Rect;

import java.util.ArrayList;

/**
 * Created by owner on 19/11/2015.
 */



public class VirtualView {

    public String type = "";
    public String text = "";
    public String viewIdResorceName = "";

    public Rect boundsInParentRect = null;
    public Rect boundsInScreenRect = null;

    public VirtualView parent = null;
    public ArrayList<VirtualView> childViews = null;

    public String _connectedTexts = ToString();

    public VirtualView(String _type,String _viewIdResorceName, String _text, Rect _boundsInParentRect,Rect _boundsInScreenRect, VirtualView _parent)
    {
        type= _type;
        viewIdResorceName = _viewIdResorceName;
        text = _text.trim();
        parent = _parent;
        boundsInParentRect = _boundsInParentRect;
        boundsInScreenRect = _boundsInScreenRect;
    }


    public void addChild(VirtualView virtualView)
    {
        if(childViews == null)
            childViews = new ArrayList<VirtualView>();

        virtualView.parent = this;

        childViews.add(virtualView);
    }

    public String ToString()
    {

        StringBuilder sb = new StringBuilder();
        if (text != "") {
            sb.append(text);
            sb.append("\n");
        }
        if(childViews != null && childViews.size()>0)
        {
            for (int i=0;i<childViews.size();i++)
            {
                String t = childViews.get(i).ToString().trim();
                if (t != "")
                {
                    sb.append(t);
                    sb.append("\n");
                }
            }
        }

        return sb.toString();
    }

    public Bitmap DrawBitmap(Bitmap bitmap)
    {

        Canvas canvas = new Canvas(bitmap);
        canvas.drawBitmap(bitmap, new Matrix(), null);

        Paint p = new Paint();

        if(text == "") {
            p.setColor(Color.BLACK);
            p.setStyle(Paint.Style.STROKE);
            p.setStrokeWidth(3);
            canvas.drawRect(boundsInScreenRect, p);
        }
        else
        {
            p.setColor(Color.RED);

            p.setTextSize(25);
            //p.setStyle(Paint.Style.FILL);
           // p.setShadowLayer(10f, 10f, 10f, Color.BLACK);

            //Rect rectText = new Rect();
            //p.getTextBounds(text, 0, text.length(), rectText);
            //canvas.drawText(text, 0, rectText.height(), p);

            //newCanvas.drawText(captionString,
             //       0, rectText.height(), paintText);

            canvas.drawText(text, boundsInScreenRect.left, boundsInScreenRect.top, p);
            p.setStyle(Paint.Style.STROKE);
            canvas.drawRect(boundsInScreenRect, p);
        }

        if(childViews != null && childViews.size()>0)
        {
            for (int i=0;i<childViews.size();i++)
            {
                childViews.get(i).DrawBitmap(bitmap);
            }
        }

        return bitmap;
    }


    public String GetXml()
    {

        StringBuilder sb = new StringBuilder();

        sb.append("<View>");

        sb.append("<Text>");
        sb.append(text);
        sb.append("</Text>");

        sb.append("<Type>");
        sb.append(type);
        sb.append("</Type>");

        sb.append("<ViewIdResorceName>");
        sb.append(viewIdResorceName);
        sb.append("</ViewIdResorceName>");

        sb.append("<boundsInScreenRect>");
        sb.append(boundsInScreenRect.left + "," + boundsInScreenRect.top + "," + boundsInScreenRect.width() + "," + boundsInScreenRect.height());
        sb.append("</boundsInScreenRect>");

        sb.append("<boundsInParentRect>");
        sb.append(boundsInParentRect.left + "," + boundsInParentRect.top + "," + boundsInParentRect.width() + "," + boundsInParentRect.height());
        sb.append("</boundsInParentRect>");

        sb.append("<ChildViews>");
        if(childViews != null && childViews.size()>0)
        {
            for (int i=0;i<childViews.size();i++)
            {
                sb.append(childViews.get(i).GetXml());
            }
        }
        sb.append("</ChildViews>");

        sb.append("</View>");

        return sb.toString();
    }
}
