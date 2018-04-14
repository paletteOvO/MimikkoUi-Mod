package me.manhong2112.mimikkouimod.layout

import android.animation.Animator
import android.app.Activity
import android.content.Context
import android.graphics.*
import android.support.v4.content.ContextCompat
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import me.manhong2112.mimikkouimod.R
import me.manhong2112.mimikkouimod.common.Utils.log
import org.jetbrains.anko.dip
import org.jetbrains.anko.sp
import org.jetbrains.anko.wrapContent


class BubbleSeekBar : SeekBar {
   constructor(context: Context) : super(context, null)
   constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, android.R.attr.seekBarStyle)

   var progressText: String = ""
   private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null
   private val mBubble = Bubble(context)

   init {
      setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> {
               val thumbPos = getThumbPosRelative()
               val w = thumb.intrinsicWidth
               val h = thumb.intrinsicHeight
               val rect = Rect(thumbPos.first - w / 2, thumbPos.second - h / 2, thumbPos.first - w / 2 + w, thumbPos.second - h / 2 + h)
               if (rect.contains(e.x.toInt(), e.y.toInt())) {
                  showBubble()
                  return@setOnTouchListener true
               }
            }
         }
         false
      }
      super.setOnSeekBarChangeListener(object : OnSeekBarChangeListener {
         override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
            mOnSeekBarChangeListener?.onProgressChanged(seekBar, progress, fromUser)
            moveBubble()
         }

         override fun onStartTrackingTouch(seekBar: SeekBar?) {
            mOnSeekBarChangeListener?.onStartTrackingTouch(seekBar)
            showBubble()
            moveBubble()
         }

         override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mOnSeekBarChangeListener?.onStopTrackingTouch(seekBar)
            hideBubble()
         }

      })
   }

   override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
      mOnSeekBarChangeListener = l
   }

   private fun showBubble() {
      if (((context as Activity).window.decorView as ViewGroup).findViewById<Bubble?>(mBubble.id) != null) {
         return
      }
      ((context as Activity).window.decorView as ViewGroup).addView(mBubble, WindowManager.LayoutParams(wrapContent, wrapContent))
      mBubble.alpha = 1f
      mBubble.scaleX = 0f
      mBubble.scaleY = 0f
      mBubble.pivotX = mBubble.mBubbleRadius / 2f * 3f
      mBubble.pivotY = mBubble.mBubbleRadius * 3f
      mBubble.animate().scaleX(1f).scaleY(1f).setDuration(130).setListener(
            object : Animator.AnimatorListener {
               override fun onAnimationRepeat(animation: Animator?) {
               }

               override fun onAnimationEnd(animation: Animator?) {
               }

               override fun onAnimationCancel(animation: Animator?) {
               }

               override fun onAnimationStart(animation: Animator?) {
                  moveBubble()
               }
            }
      ).start()
   }

   private fun moveBubble() {
      mBubble.x = getThumbPosOnScreen().first.toFloat() - (mBubble.mBubbleRadius * 3 / 2)
      mBubble.y = getThumbPosOnScreen().second.toFloat() - mBubble.mBubbleRadius * 3 - dip(5)
      mBubble.invalidate()
   }

   private fun hideBubble() {
      mBubble.animate().alpha(0f).scaleX(0.8f).scaleY(0.8f).setDuration(100).setListener(
            object : Animator.AnimatorListener {
               override fun onAnimationRepeat(animation: Animator?) {
               }

               override fun onAnimationEnd(animation: Animator?) {
                  ((context as Activity).window.decorView as ViewGroup).removeView(mBubble)
               }

               override fun onAnimationCancel(animation: Animator?) {
                  ((context as Activity).window.decorView as ViewGroup).removeView(mBubble)
               }

               override fun onAnimationStart(animation: Animator?) {
               }

            }
      ).start()
   }

   private fun getThumbPosOnScreen(): Pair<Int, Int> {
      return thumb.bounds.let {
         val arr: IntArray = intArrayOf(0, 0)
         getLocationOnScreen(arr)
         (arr[0] + it.centerX() + thumb.intrinsicWidth / 2 - 6 to arr[1] + it.centerY()).also { // why 6 ??
            value ->
            log(value.toString())
            log("${arr[0]}, ${arr[1]}")
            log("${it.centerX()}")
            log("${thumb.intrinsicWidth / 2}")
         }
      }
   }

   private fun getThumbPosRelative(): Pair<Int, Int> {
      return thumb.bounds.let {
         it.centerX() to it.centerY()
      }
   }


   inner class Bubble(private val ctx: Context) : View(ctx) {
      private val mBubblePath = Path()
      private val mBubbleRectF = RectF()
      private val mRect = Rect()
      val mBubbleRadius = 48f
      private val mBubblePaint = Paint()
      private val mBubbleColor = ContextCompat.getColor(ctx, R.color.colorAccent)
      private val mBubbleTextColor = Color.WHITE

      init {
         id = View.generateViewId()
         mBubblePaint.isAntiAlias = true
         mBubblePaint.textAlign = Paint.Align.CENTER
      }

      override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
         super.onMeasure(widthMeasureSpec, heightMeasureSpec)

         setMeasuredDimension(3 * mBubbleRadius.toInt(), 3 * mBubbleRadius.toInt())

         mBubbleRectF.set(measuredWidth / 2f - mBubbleRadius, 0f,
               measuredWidth / 2f + mBubbleRadius, 2 * mBubbleRadius)
      }

      override fun onDraw(canvas: Canvas) {
         super.onDraw(canvas)

         mBubblePath.reset()
         val x0 = measuredWidth / 2f
         val y0 = measuredHeight - mBubbleRadius / 3f
         mBubblePath.moveTo(x0, y0)
         val x1 = (measuredWidth / 2f - Math.sqrt(3.0) / 2f * mBubbleRadius).toFloat()
         val y1 = 3 / 2f * mBubbleRadius
         mBubblePath.quadTo(
               x1 - dip(2), y1 - dip(2),
               x1, y1
         )
         mBubblePath.arcTo(mBubbleRectF, 150f, 240f)

         val x2 = (measuredWidth / 2f + Math.sqrt(3.0) / 2f * mBubbleRadius).toFloat()
         mBubblePath.quadTo(
               x2 + dip(2), y1 - dip(2),
               x0, y0
         )
         mBubblePath.close()

         mBubblePaint.color = mBubbleColor
         canvas.drawPath(mBubblePath, mBubblePaint)

         mBubblePaint.textSize = sp(12).toFloat()
         mBubblePaint.color = mBubbleTextColor

         mBubblePaint.getTextBounds(progressText, 0, progressText.length, mRect)
         val fm = mBubblePaint.fontMetrics
         val baseline = mBubbleRadius + (fm.descent - fm.ascent) / 2f - fm.descent
         canvas.drawText(progressText, measuredWidth / 2f, baseline, mBubblePaint)
      }
   }
}