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

package org.kiwix.kiwixmobile.page.history

import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.page.PageScreenRoute
import org.kiwix.kiwixmobile.core.page.history.viewmodel.HistoryViewModel
import org.kiwix.kiwixmobile.core.utils.datastore.KiwixDataStore
import org.kiwix.kiwixmobile.core.utils.dialog.AlertDialogShower

/**
 * History screen route for Kiwix app.
 * This uses the generic PageScreenRoute with HistoryViewModel.
 */
@Composable
fun HistoryScreenRoute(
  navigateBack: () -> Unit,
  historyViewModel: HistoryViewModel,
  kiwixDataStore: KiwixDataStore,
  alertDialogShower: AlertDialogShower
) {
  PageScreenRoute(
    navigateBack = navigateBack,
    pageViewModel = historyViewModel,
    screenTitleRes = R.string.history,
    noItemsString = stringResource(R.string.no_history),
    switchString = stringResource(R.string.history_from_current_book),
    searchQueryHint = stringResource(R.string.search_history),
    deleteIconTitleRes = R.string.pref_clear_all_history_title,
    switchIsCheckedFlow = kiwixDataStore.showHistoryOfAllBooks,
    alertDialogShower = alertDialogShower,
    pageViewModelClickListener = null
  )
}
