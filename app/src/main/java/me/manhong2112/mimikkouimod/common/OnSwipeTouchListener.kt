package me.manhong2112.mimikkouimod.common

import android.content.Context
import android.view.GestureDetector
import android.view.GestureDetector.SimpleOnGestureListener
import android.view.MotionEvent
import android.view.View
import android.view.View.OnTouchListener


open class OnSwipeTouchListener(ctx: Context) : OnTouchListener {
   companion object {
      private const val SWIPE_THRESHOLD = 100
      private const val SWIPE_VELOCITY_THRESHOLD = 100
   }

   private val gestureDetector: GestureDetector

   init {
      gestureDetector = GestureDetector(ctx, GestureListener())
   }

   override fun onTouch(v: View, event: MotionEvent): Boolean {
      return gestureDetector.onTouchEvent(event)
   }

   private inner class GestureListener : SimpleOnGestureListener() {

      override fun onDown(e: MotionEvent): Boolean {
         return true
      }

      override fun onSingleTapUp(e: MotionEvent): Boolean {
         onClick()
         return super.onSingleTapUp(e)
      }

      override fun onDoubleTap(e: MotionEvent): Boolean {
         onDoubleClick()
         return super.onDoubleTap(e)
      }

      override fun onLongPress(e: MotionEvent) {
         onLongClick()
         super.onLongPress(e)
      }

      override fun onFling(e1: MotionEvent, e2: MotionEvent, velocityX: Float, velocityY: Float): Boolean {
         val diffY = e2.y - e1.y
         val diffX = e2.x - e1.x
         if (Math.abs(diffX) > Math.abs(diffY)) {
            if (Math.abs(diffX) > SWIPE_THRESHOLD && Math.abs(velocityX) > SWIPE_VELOCITY_THRESHOLD) {
               if (diffX > 0) {
                  onSwipeRight()
               } else {
                  onSwipeLeft()
               }
               return true
            }
         } else if (Math.abs(diffY) > SWIPE_THRESHOLD && Math.abs(velocityY) > SWIPE_VELOCITY_THRESHOLD) {
            if (diffY > 0) {
               onSwipeBottom()
            } else {
               onSwipeTop()
            }
            return true
         }

         return false
      }
   }

   open fun onSwipeRight() {}
   open fun onSwipeLeft() {}
   open fun onSwipeTop() {}
   open fun onSwipeBottom() {}
   open fun onClick() {}
   open fun onDoubleClick() {}
   open fun onLongClick() {}
}