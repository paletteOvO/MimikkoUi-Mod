package me.manhong2112.mimikkouimod.common

import org.json.JSONArray
import java.util.*

class JSONList<T>(val jsonArray: JSONArray) : List<T>, java.io.Serializable {
   override val size: Int
      get() = jsonArray.length()

   override fun contains(element: T): Boolean {
      return indexOf(element) != -1
   }


   override fun containsAll(elements: Collection<T>): Boolean {
      elements.forEach { ele ->
         if (!this.contains(ele)) {
            return false
         }
      }
      return true
   }

   override fun get(index: Int): T {
      return jsonArray.get(index) as T
   }

   override fun indexOf(element: T): Int {
      for (i in 0 until size) {
         if (jsonArray.get(i) == element) {
            return i
         }
      }
      return -1
   }

   override fun isEmpty(): Boolean {
      return size == 0
   }

   override fun iterator(): Iterator<T> {
      return listIterator()
   }

   override fun lastIndexOf(element: T): Int {
      for (i in size - 1..0) {
         if (jsonArray.get(i) == element) {
            return i
         }
      }
      return -1
   }

   override fun listIterator(): ListIterator<T> {
      return listIterator(0)
   }

   override fun listIterator(index: Int): ListIterator<T> {
      return object : ListIterator<T> {
         var mIndex = 0
         override fun hasNext(): Boolean {
            return mIndex < size
         }

         override fun hasPrevious(): Boolean {
            return mIndex > 0
         }

         override fun next(): T {
            if (hasNext())
               return get(mIndex++)
            else
               throw NoSuchElementException()
         }

         override fun nextIndex(): Int {
            return mIndex
         }

         override fun previous(): T {
            if (hasPrevious())
               return get(--mIndex)
            else
               throw NoSuchElementException()
         }

         override fun previousIndex(): Int {
            return mIndex - 1
         }
      }
   }

   override fun subList(fromIndex: Int, toIndex: Int): List<T> {
      TODO("not implemented") //To change body of created functions use File | Settings | File Templates.
   }
}