/***
 * Excerpted from "Hello, Android! 3e",
 * published by The Pragmatic Bookshelf.
 * Copyrights apply to this code. It may not be used to create training material, 
 * courses, books, articles, and the like. Contact us if you are in doubt.
 * We make no guarantees that this code is fit for any purpose. 
 * Visit http://www.pragmaticprogrammer.com/titles/eband3 for more book information.
***/
package com.buuuk.android.ui.touch;

import android.view.MotionEvent;

public class EclairMotionEvent extends WrapMotionEvent {

   protected EclairMotionEvent(MotionEvent event) {
      super(event);
   }

   public float getX(int pointerIndex) {
      return event.getX(pointerIndex);
   }

   public float getY(int pointerIndex) {
      return event.getY(pointerIndex);
   }

   public int getPointerCount() {
      return event.getPointerCount();
   }

   public int getPointerId(int pointerIndex) {
      return event.getPointerId(pointerIndex);
   }
}
