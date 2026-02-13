/*
 * Kiwix Android
 * Copyright (c) 2025 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.page.bookmarks

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.page.PageScreenRoute
import org.kiwix.kiwixmobile.core.page.bookmark.viewmodel.BookmarkViewModel
import org.kiwix.kiwixmobile.core.utils.datastore.KiwixDataStore
import org.kiwix.kiwixmobile.core.utils.dialog.AlertDialogShower

/**
 * Bookmarks screen route for Kiwix app.
 * This uses the generic PageScreenRoute with BookmarkViewModel.
 */
@Composable
fun BookmarksScreenRoute(
  navigateBack: () -> Unit,
  bookmarkViewModel: BookmarkViewModel,
  kiwixDataStore: KiwixDataStore,
  alertDialogShower: AlertDialogShower
) {
  PageScreenRoute(
    navigateBack = navigateBack,
    pageViewModel = bookmarkViewModel,
    screenTitleRes = R.string.bookmarks,
    noItemsString = stringResource(R.string.no_bookmarks),
    switchString = stringResource(R.string.bookmarks_from_current_book),
    searchQueryHint = stringResource(R.string.search_bookmarks),
    deleteIconTitleRes = R.string.pref_clear_all_bookmarks_title,
    switchIsCheckedFlow = kiwixDataStore.showBookmarksOfAllBooks,
    alertDialogShower = alertDialogShower,
    pageViewModelClickListener = null
  )
}
