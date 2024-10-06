/*
 * Kiwix Android
 * Copyright (c) 2020 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.localFileTransfer

import android.util.Log
import androidx.test.espresso.Espresso.onView
import androidx.test.espresso.action.ViewActions.click
import androidx.test.espresso.assertion.ViewAssertions.doesNotExist
import androidx.test.espresso.assertion.ViewAssertions.matches
import androidx.test.espresso.matcher.ViewMatchers.isDisplayed
import androidx.test.espresso.matcher.ViewMatchers.withId
import androidx.test.espresso.matcher.ViewMatchers.withText
import applyWithViewHierarchyPrinting
import com.adevinta.android.barista.interaction.BaristaSleepInteractions
import org.kiwix.kiwixmobile.BaseRobot
import org.kiwix.kiwixmobile.Findable.StringId.TextId
import org.kiwix.kiwixmobile.Findable.ViewId
import org.kiwix.kiwixmobile.R
import org.kiwix.kiwixmobile.core.R.string
import org.kiwix.kiwixmobile.testutils.TestUtils
import org.kiwix.kiwixmobile.testutils.TestUtils.testFlakyView
import uk.co.deanwild.materialshowcaseview.R.id

/**
 * Authored by Ayush Shrivastava on 29/10/20
 */

fun localFileTransfer(func: LocalFileTransferRobot.() -> Unit) =
  LocalFileTransferRobot().applyWithViewHierarchyPrinting(func)

class LocalFileTransferRobot : BaseRobot() {

  fun assertReceiveFileTitleVisible() {
    isVisible(TextId(R.string.receive_files_title))
  }

  fun assertSearchDeviceMenuItemVisible() {
    isVisible(ViewId(R.id.menu_item_search_devices))
  }

  fun clickOnSearchDeviceMenuItem() {
    clickOn(ViewId(R.id.menu_item_search_devices))
  }

  fun assertLocalFileTransferScreenVisible() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS_FOR_DOWNLOAD_TEST.toLong())
    closeEnableWifiP2PDialogIfVisible()
    assertReceiveFileTitleVisible()
  }

  private fun closeEnableWifiP2PDialogIfVisible() {
    try {
      testFlakyView({
        onView(withText(string.request_enable_wifi)).check(matches(isDisplayed()))
        pressBack()
      })
    } catch (ignore: Throwable) {
      Log.i(
        "LOCAL_FILE_TRANSFER_TEST",
        "Couldn't found WIFI P2P dialog, probably this is not exist"
      )
    }
  }

  fun assertLocalLibraryVisible() {
    isVisible(TextId(string.library))
  }

  fun assertClickNearbyDeviceMessageVisible() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onView(withId(id.tv_content))
        .check(matches(withText(string.click_nearby_devices_message)))
    })
  }

  fun clickOnGotItButton() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onView(withId(id.tv_dismiss))
        .perform(click())
    })
  }

  fun assertDeviceNameMessageVisible() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onView(withId(id.tv_content))
        .check(matches(withText(string.your_device_name_message)))
    })
  }

  fun assertNearbyDeviceListMessageVisible() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onView(withId(id.tv_content))
        .check(matches(withText(string.nearby_devices_list_message)))
    })
  }

  fun assertTransferZimFilesListMessageVisible() {
    pauseForBetterTestPerformance()
    testFlakyView({
      onView(withId(id.tv_content))
        .check(matches(withText(string.transfer_zim_files_list_message)))
    })
  }

  fun assertClickNearbyDeviceMessageNotVisible() {
    pauseForBetterTestPerformance()
    onView(withText(string.click_nearby_devices_message)).check(doesNotExist())
  }

  private fun pauseForBetterTestPerformance() {
    BaristaSleepInteractions.sleep(TestUtils.TEST_PAUSE_MS.toLong())
  }
}
