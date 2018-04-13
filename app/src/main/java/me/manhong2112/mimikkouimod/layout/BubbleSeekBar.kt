package me.manhong2112.mimikkouimod.layout

import android.app.Activity
import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.SeekBar
import me.manhong2112.mimikkouimod.common.Utils.log
import org.jetbrains.anko.dip
import org.jetbrains.anko.opaque
import org.jetbrains.anko.sp
import org.jetbrains.anko.wrapContent


class BubbleSeekBar : SeekBar {
   constructor(context: Context) : super(context, null)
   constructor(context: Context, attrs: AttributeSet?) : super(context, attrs, android.R.attr.seekBarStyle)

   init {
      setOnTouchListener { v, e ->
         when (e.action) {
            MotionEvent.ACTION_DOWN -> {
               showBubble()
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

         }

         override fun onStopTrackingTouch(seekBar: SeekBar?) {
            mOnSeekBarChangeListener?.onStopTrackingTouch(seekBar)
            hideBubble()
         }

      })
   }

   private var mOnSeekBarChangeListener: OnSeekBarChangeListener? = null
   private val mBubble by lazy {
      Bubble(context)
   }

   override fun setOnSeekBarChangeListener(l: OnSeekBarChangeListener?) {
      mOnSeekBarChangeListener = l
   }

   private fun showBubble() {
      ((context as Activity).window.decorView as ViewGroup).addView(mBubble, WindowManager.LayoutParams(wrapContent, wrapContent))
      moveBubble()
   }

   private fun moveBubble() {
      mBubble.x = getThumbPos().first.toFloat() - (mBubble.width / 2)
      mBubble.y = getThumbPos().second.toFloat() - mBubble.height - dip(5)
      mBubble.invalidate()
   }

   private fun hideBubble() {
      ((context as Activity).window.decorView as ViewGroup).removeView(mBubble)
   }

   private fun getThumbPos(): Pair<Int, Int> {
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

   inner class Bubble(private val ctx: Context) : View(ctx) {
      val mBubblePath = Path()
      val mBubbleRectF = RectF()
      val mRect = Rect()
      val mBubbleRadius = 50f
      val mBubblePaint = Paint()
      val mBubbleColor = 0xe94f67.opaque
      val mBubbleTextColor = 0xffffff.opaque

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

         val prog = progress.toString()
         mBubblePaint.getTextBounds(prog, 0, prog.length, mRect)
         val fm = mBubblePaint.fontMetrics
         val baseline = mBubbleRadius + (fm.descent - fm.ascent) / 2f - fm.descent
         canvas.drawText(prog, measuredWidth / 2f, baseline, mBubblePaint)
      }
   }
}