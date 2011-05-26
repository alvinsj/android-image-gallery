/***
 * Excerpted from "Hello, Android! 3e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package com.buuuk.android.ui.touch;

import com.buuuk.android.util.Geometry;

import android.R;
import android.app.Activity;
import android.graphics.Matrix;
import android.graphics.PointF;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnTouchListener;
import android.widget.ImageView;

public abstract class TouchActivity extends Activity {
   private static final String TAG = "Touch";
   // These matrices will be used to move and zoom image
   protected Matrix matrix = new Matrix();
   Matrix savedMatrix = new Matrix();

   // We can be in one of these 3 states
   static final int NONE = 0;
   static final int DRAG = 1;
   static final int ZOOM = 2;
   int mode = NONE;

   // Remember some things for zooming
   PointF start = new PointF();
   PointF refLineStart = null;
   PointF refLineEnd = null;
   PointF prevLineStart = null;
   PointF prevLineEnd = null;
   PointF newStart = new PointF();
   PointF newEnd = new PointF();
   float oldAngle = 0f;
   
   PointF mid = new PointF();
   float oldDist = 1f;
   Float mRotateAngle = 0f;
   float scaleFactor = 0f;

   @Override
   public void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      //setContentView(getContentView());
      //ImageView view = getImageView();
      //view.setOnTouchListener(this);

      // ...
      // Work around a Cupcake bug
      matrix.setTranslate(1f, 1f);
      //view.setImageMatrix(matrix);
   }
   
   public abstract float getMinZoomScale();
   
   public boolean onTouchEvented(View v, MotionEvent rawEvent) {
	 
      WrapMotionEvent event = WrapMotionEvent.wrap(rawEvent);
      // ...
      ImageView view = (ImageView)v;

      // Dump touch event to log
      dumpEvent(event);

      // Handle touch events here...
      switch (event.getAction() & MotionEvent.ACTION_MASK) {
      case MotionEvent.ACTION_DOWN:
         savedMatrix.set(matrix);
         start.set(event.getX(), event.getY());
         Log.d(TAG, "mode=DRAG");
         mode = DRAG;
         break;
      case MotionEvent.ACTION_POINTER_DOWN:
         oldDist = spacing(event);  
         Log.d(TAG, "oldDist=" + oldDist);
         if (oldDist > 10f) {
            savedMatrix.set(matrix);
            midPoint(mid, event);
            mode = ZOOM;
            Log.d(TAG, "mode=ZOOM");
            
            // set the old line if first attempt
            refLineStart = new PointF();
            refLineEnd = new PointF();
            refLineStart.set(event.getX(0), event.getY(0));
            refLineEnd.set(event.getX(1), event.getY(1));
            prevLineStart = new PointF();
            prevLineEnd = new PointF();
            prevLineStart.set(event.getX(0), event.getY(0));
            prevLineEnd.set(event.getX(1), event.getY(1));
         }
         break;
      case MotionEvent.ACTION_UP:
      case MotionEvent.ACTION_POINTER_UP:
         mode = NONE;
         Log.d(TAG, "mode=NONE");
         refLineStart = null;
         refLineEnd = null;
         mRotateAngle = 0f;
         break;
      case MotionEvent.ACTION_MOVE:
         if (mode == DRAG) {
            // ...
            matrix.set(savedMatrix);
            matrix.postTranslate(event.getX() - start.x,
                  event.getY() - start.y);
         }
         else if (mode == ZOOM) {
            float newDist = spacing(event);
            Log.d(TAG, "newDist=" + newDist);
            if (newDist > 10f) {
               matrix.set(savedMatrix);
               float scale = newDist / oldDist;
               
               Log.v("Scaling","Scale: "+scale);
               
               Matrix test = new Matrix(matrix);
               
              
               // test scale matrix 
               test.postScale(scale, scale, mid.x, mid.y);
               float[] values = new float[9];
               test.getValues(values);
               
               if(values[0]>=getMinZoomScale())
            	   matrix.postScale(scale, scale, mid.x, mid.y);
               else
            	   matrix.setScale(getMinZoomScale(), getMinZoomScale(), mid.x, mid.y);
               
               // get the latest line
               newStart.set(event.getX(0), event.getY(0));
               newEnd.set(event.getX(1), event.getY(1));
               
               float angle = new Float(angleBetweenLinesInRadians(prevLineStart,prevLineEnd,newStart,newEnd)).floatValue();
            
               
               
               // calculate the angle difference and do rotation
               
               mRotateAngle = mRotateAngle + angle;
               
               //matrix.postRotate( mRotateAngle ,mid.x, mid.y );
            // record the old line value
               prevLineStart.set(event.getX(0), event.getY(0));
               prevLineEnd.set(event.getX(1), event.getY(1));
            }
           
         }
         break;
      }
      
      Log.v("Matrix","scale matrix: "+matrix);
      
      view.setImageMatrix(matrix);
      
      
      return true; // indicate event was handled
   }
   
   public double angleBetweenLinesInRadians( PointF line1Start, PointF line1End, PointF line2Start, PointF line2End) {
       
       float a = line1End.x - line1Start.x;
       float b = line1End.y - line1Start.y;
       float c = line2End.x - line2Start.x;
       float d = line2End.y - line2Start.y;
       
       float line1Slope = (line1End.y - line1Start.y) / (line1End.x - line1Start.x);
       float line2Slope = (line2End.y - line2Start.y) / (line2End.x - line2Start.x);
       
       double subf = ((a*c) + (b*d)) / ((Math.sqrt(a*a + b*b)) * (Math.sqrt(c*c + d*d)));

       subf = subf>1?1:subf;
       subf = subf<-1?-1:subf;
       double degs = Math.acos(subf);
       
       double result = (line2Slope > line1Slope) ? degs*180/3.142 : -degs*180/3.142;
  
       return result;    
   }

   /** Show an event in the LogCat view, for debugging */
   private void dumpEvent(WrapMotionEvent event) {
      // ...
      String names[] = { "DOWN", "UP", "MOVE", "CANCEL", "OUTSIDE",
            "POINTER_DOWN", "POINTER_UP", "7?", "8?", "9?" };
      StringBuilder sb = new StringBuilder();
      int action = event.getAction();
      int actionCode = action & MotionEvent.ACTION_MASK;
      sb.append("event ACTION_").append(names[actionCode]);
      if (actionCode == MotionEvent.ACTION_POINTER_DOWN
            || actionCode == MotionEvent.ACTION_POINTER_UP) {
         sb.append("(pid ").append(
               action >> MotionEvent.ACTION_POINTER_ID_SHIFT);
         sb.append(")");
      }
      sb.append("[");
      for (int i = 0; i < event.getPointerCount(); i++) {
         sb.append("#").append(i);
         sb.append("(pid ").append(event.getPointerId(i));
         sb.append(")=").append((int) event.getX(i));
         sb.append(",").append((int) event.getY(i));
         if (i + 1 < event.getPointerCount())
            sb.append(";");
      }
      sb.append("]");
      Log.d(TAG, sb.toString());
   }

   /** Determine the space between the first two fingers */
   private float spacing(WrapMotionEvent event) {
      // ...
      float x = event.getX(0) - event.getX(1);
      float y = event.getY(0) - event.getY(1);
      return FloatMath.sqrt(x * x + y * y);
   }

   /** Calculate the mid point of the first two fingers */
   private void midPoint(PointF point, WrapMotionEvent event) {
      // ...
      float x = event.getX(0) + event.getX(1);
      float y = event.getY(0) + event.getY(1);
      point.set(x / 2, y / 2);
   }
}
