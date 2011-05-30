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
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.FloatMath;
import android.util.Log;
import android.view.Display;
import android.view.MotionEvent;
import android.view.Surface;
import android.view.View;
import android.view.WindowManager;
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
   public abstract void resetImage(ImageView iv, Drawable draw);

   
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
         
         boolean isScaleChanged=true;
         float[] values = new float[9];
 	     view.getImageMatrix().getValues(values);
 		 if(values[0]<=getMinZoomScale()) {
 			 resetImage(view,view.getDrawable());
 			 isScaleChanged=false;
 		 }
 		 
 		 
 		 
 		 Rect rect = new Rect();
 		 view.getDrawingRect(rect);
 		 Log.d("ImageView","Drawing Rect: "+rect.top+","+rect.right+","+rect.bottom+","+rect.left);
 		 
 		 float[] point = new float[2];
 		 
 		//for top and left
 		 point[0]= rect.left;
 		 point[1]= rect.top;
 		 float[] topleft = new float[2]; 
 		 view.getImageMatrix().mapPoints(topleft,point); // topleft image point after applying matrix
 		
 		 point[0] = view.getDrawable().getIntrinsicWidth();
		 point[1] = view.getDrawable().getIntrinsicHeight();
 		 float[] bottomright = new float[2];
		 view.getImageMatrix().mapPoints(bottomright,point); //bottomright image point after applying matrix
 		 
		 
		 // get orientation
		 Display display = ((WindowManager) getSystemService(WINDOW_SERVICE)).getDefaultDisplay();
		 int rotation = display.getRotation();
		
		 int orientation = 0;
		 if( rotation == Surface.ROTATION_0 || rotation == Surface.ROTATION_180)
			orientation = 0;
		 else
			orientation = 1;
		 
		 
		 boolean direction = true;
		 
 		 float width = bottomright[0]-topleft[0];
 		 float height = bottomright[1]-topleft[1];
 		 
 		 
	 	 if( (width>rect.right && height>rect.bottom)  )
	 		direction=true;
	 	 else
	 		direction=false;
	 	    
 		
		 
		 // snap to topright
 		 if(topleft[0]>rect.left && bottomright[0]>rect.right && isScaleChanged && direction){
 			matrix.postTranslate(-topleft[0], 0);	 
 		 }
 		 else if(bottomright[0]<rect.right && topleft[0]<rect.left && isScaleChanged && direction){
 			matrix.postTranslate(rect.right-bottomright[0], 0);	
 		 }
 		 else if(topleft[0]>rect.left && bottomright[0]>rect.right && isScaleChanged && !direction){
 			matrix.postTranslate(-(bottomright[0]-rect.right), 0);	 
 		 }
 		 else if(bottomright[0]<rect.right && topleft[0]<rect.left && isScaleChanged && !direction){
 			matrix.postTranslate(-(topleft[0]-rect.left), 0);	
 		 }
 		 
 		 
 		 if(bottomright[1]>rect.bottom && topleft[1]>rect.top && isScaleChanged && direction){
 			matrix.postTranslate(0, -topleft[1]);
 		 }
 		 else if(bottomright[1]<rect.bottom && topleft[1]<rect.top && isScaleChanged && direction){
  			matrix.postTranslate(0, rect.bottom-bottomright[1]);
 		 }
 		 else if(bottomright[1]>rect.bottom && topleft[1]>rect.top && isScaleChanged && !direction){
 			matrix.postTranslate(0, -(bottomright[1]-rect.bottom));
 		 }
 		 else if(bottomright[1]<rect.bottom && topleft[1]<rect.top && isScaleChanged && !direction){
  			matrix.postTranslate(0, -(topleft[1]-rect.top));
 		 }
 		 
 		 Log.d("ImageView", "Map points source "+point[0]+","+point[1]+ " to topleft:"+topleft[0]+","+topleft[1]+" and bottomright:"+bottomright[0]+","+bottomright[1]);
 		 
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
               float[] svalues = new float[9];
               test.getValues(svalues);
               
               //if(svalues[0]>=getMinZoomScale())
            	   matrix.postScale(scale, scale, mid.x, mid.y);
               //else
            	 //  matrix.setScale(getMinZoomScale(), getMinZoomScale(), mid.x, mid.y);
               
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
      
     
      System.gc();
      
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
