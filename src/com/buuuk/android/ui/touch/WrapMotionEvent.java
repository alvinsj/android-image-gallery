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

public class WrapMotionEvent {
   protected MotionEvent event;
   
   
   

   protected WrapMotionEvent(MotionEvent event) {
      this.event = event;
   }

   static public WrapMotionEvent wrap(MotionEvent event) {
      try {
         return new EclairMotionEvent(event);
      } catch (VerifyError e) {
         return new WrapMotionEvent(event);
      }
   }
   
   

   public int getAction() {
      return event.getAction();
   }

   public float getX() {
      return event.getX();
   }

   public float getX(int pointerIndex) {
      verifyPointerIndex(pointerIndex);
      return getX();
   }

   public float getY() {
      return event.getY();
   }

   public float getY(int pointerIndex) {
      verifyPointerIndex(pointerIndex);
      return getY();
   }

   public int getPointerCount() {
      return 1;
   }

   public int getPointerId(int pointerIndex) {
      verifyPointerIndex(pointerIndex);
      return 0;
   }

   private void verifyPointerIndex(int pointerIndex) {
      if (pointerIndex > 0) {
         throw new IllegalArgumentException(
               "Invalid pointer index for Donut/Cupcake");
      }
   }
   
}

