/*
 * Kiwix Android
 * Copyright (c) 2019 Kiwix <android.kiwix.org>
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 *
 */

import android.app.Activity
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.test.platform.app.InstrumentationRegistry
import androidx.test.runner.lifecycle.ActivityLifecycleMonitorRegistry
import androidx.test.runner.lifecycle.Stage.RESUMED
import androidx.viewpager.widget.ViewPager
import org.kiwix.kiwixmobile.BaseRobot
import java.io.File

inline fun <reified T : BaseRobot> T.applyWithViewHierarchyPrinting(
  crossinline function: T.() -> Unit
): T =
  apply {
    try {
      function()
    } catch (runtimeException: RuntimeException) {
      uiDevice.takeScreenshot(File(context.filesDir, "${System.currentTimeMillis()}.png"))
      InstrumentationRegistry.getInstrumentation().runOnMainSync {
        // During the Pause state, it can't any state or since it may not hold strong reference it
        // may garbage collected in the low memory stage or may not return an instance of particular
        // activity
        val activity =
          ActivityLifecycleMonitorRegistry.getInstance().getActivitiesInStage(RESUMED).lastOrNull()
        if (activity != null) {
          throw RuntimeException(
            combineMessages(
              runtimeException = runtimeException,
              activity = activity
            ),
            runtimeException
          )
        }
        throw RuntimeException()
      }
    }
  }

fun combineMessages(
  runtimeException: RuntimeException,
  activity: Activity
) = "${runtimeException.message}\n${getViewHierarchy(activity.window.decorView)}"

fun getViewHierarchy(v: View) =
  StringBuilder().apply { getViewHierarchy(v, this, 0) }.toString()

fun attempt(count: Int, function: () -> Unit): Unit =
  try {
    function.invoke()
  } catch (e: Exception) {
    if (count - 1 == 0) {
      throw e
    } else {
      attempt(count - 1, function)
    }
  }

private fun getViewHierarchy(v: View, desc: StringBuilder, margin: Int) {
  desc.append(getViewMessage(v, margin))
  if (v is ViewGroup) {
    for (i in 0 until v.childCount) {
      getViewHierarchy(v.getChildAt(i), desc, margin + 1)
    }
  }
}

private fun getViewMessage(v: View, marginOffset: Int) =
  "${numSpaces(marginOffset)}[${v.javaClass.simpleName}]${resourceId(v)}${text(v)}" +
    "${contentDescription(v)}${visibility(v)}${page(v)}\n"

fun page(v: View) = if (v is ViewPager) " page: ${v.currentItem}" else ""

fun visibility(v: View) =
  " visibility:" +
    when (v.visibility) {
      View.VISIBLE -> "visible"
      View.INVISIBLE -> "invisible"
      else -> "gone"
    }

fun contentDescription(view: View) =
  view.contentDescription?.let {
    if (it.isNotEmpty()) " contDesc:$it" else null
  }.orEmpty()

fun text(v: View) =
  if (v is TextView) {
    if (v.text.isNotEmpty()) " text:${v.text}" else ""
  } else {
    ""
  }

private fun resourceId(view: View) =
  if (view.id > 0 && view.resources != null) {
    " id:${view.resources.getResourceName(view.id)}"
  } else {
    ""
  }

private fun numSpaces(marginOffset: Int) = (0..marginOffset).fold("") { acc, _ -> "$acc-" }
