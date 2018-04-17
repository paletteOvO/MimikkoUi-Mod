package me.manhong2112.mimikkouimod.layout

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Rect
import android.graphics.drawable.BitmapDrawable
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import android.widget.BaseAdapter
import android.widget.ListView
import me.manhong2112.mimikkouimod.common.Utils.log
import java.lang.ref.WeakReference
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty


class DragAndDropListView<T> : ListView {
   private val ctx: Context

   constructor(ctx: Context) : super(ctx) {
      this.ctx = ctx
      init(ctx)
   }

   constructor(ctx: Context, attrs: AttributeSet) : super(ctx, attrs) {
      this.ctx = ctx
      init(ctx)
   }

   constructor(ctx: Context, attrs: AttributeSet, resId: Int) : super(ctx, attrs, resId) {
      this.ctx = ctx
      init(ctx)
   }

   fun MutableList<T>.swap(i1: Int, i2: Int) {
      this[i1].let {
         this[i1] = this[i2]
         this[i2] = it
      }
   }

   private var hoverViewDrawable: BitmapDrawable? = null
   private var hoverViewBound: Rect? = null

   private var hoverView: View? by object : ReadWriteProperty<ListView?, View?> {
      private var value: WeakReference<View>? = null
      override fun getValue(thisRef: ListView?, property: KProperty<*>): View? {
         return value?.get()
      }

      override fun setValue(thisRef: ListView?, property: KProperty<*>, value: View?) {
         value ?: run {
            this.value = null
            return
         }
         this.value = WeakReference(value)
      }

   }
   private var hoverViewPos: Int = -1

   var onItemSwapListener = { _: Int, _: Int -> }

   var targetList: MutableList<T>? = null

   private fun init(ctx: Context) {
      setOnItemLongClickListener { parent, view, position, id ->
         createHoverViewDrawable(view)
         view.visibility = View.INVISIBLE
         hoverView = view
         hoverViewPos = position
         true
      }
   }


   private var lastX = -1
   private var lastY = -1

   override fun onTouchEvent(ev: MotionEvent): Boolean {
      when (ev.action) {
         MotionEvent.ACTION_DOWN -> {
            lastX = ev.x.toInt()
            lastY = ev.y.toInt()
         }
         MotionEvent.ACTION_UP -> {
            hoverViewBound?.set(0, 0, 0, 0)
            hoverView?.visibility = View.VISIBLE
            hoverViewDrawable = null
            hoverViewBound = null
            hoverViewPos = -1
         }
         MotionEvent.ACTION_MOVE -> {
            hoverViewDrawable?.let {
               val dx = ev.x.toInt() - lastX
               val dy = ev.y.toInt() - lastY
               lastX = ev.x.toInt()
               lastY = ev.y.toInt()
               it.bounds = hoverViewBound?.also {
                  it.offset(0, dy)
               }
               val rect = Rect()
               hoverView!!.getGlobalVisibleRect(rect)
               log("hoverView rect ${rect.left} ${rect.top}")

               hoverView!!.visibility = View.VISIBLE
               if (lastY < rect.top - hoverView!!.height / 2 && hoverViewPos > 0) {
                  onItemSwapListener(hoverViewPos, hoverViewPos - 1)
                  targetList?.swap(hoverViewPos, hoverViewPos - 1)
                  hoverViewPos -= 1
               } else if (lastY > rect.top + hoverView!!.height + hoverView!!.height / 2 && hoverViewPos < adapter.count - 1) {
                  onItemSwapListener(hoverViewPos, hoverViewPos + 1)
                  targetList?.swap(hoverViewPos, hoverViewPos + 1)
                  hoverViewPos += 1
               }
               (adapter as BaseAdapter).notifyDataSetChanged()
               hoverView = getChildAt(hoverViewPos - firstVisiblePosition)
               hoverView!!.visibility = View.INVISIBLE
               Unit
            }
            invalidate()
         }
         else -> {
            return super.onTouchEvent(ev)
         }
      }
      return super.onTouchEvent(ev)
   }

   private fun createHoverViewDrawable(v: View) {
      val bitmap = Bitmap.createBitmap(v.width, v.height, Bitmap.Config.ARGB_8888)
      val canvas = Canvas(bitmap)
      canvas.drawARGB(0xFF, 0xE5, 0xE5, 0xE5)
      canvas.save()
      v.draw(canvas)
      canvas.restore()
      val bitmapDrawable = BitmapDrawable(context.resources, bitmap)
      bitmapDrawable.alpha = 180
      hoverViewBound = Rect(v.left, v.top, v.left + v.width, v.top + v.height)
      hoverViewDrawable = bitmapDrawable

      bitmapDrawable.bounds = hoverViewBound
   }

   override fun dispatchDraw(canvas: Canvas) {
      super.dispatchDraw(canvas)
      hoverViewDrawable?.draw(canvas)
   }
}
