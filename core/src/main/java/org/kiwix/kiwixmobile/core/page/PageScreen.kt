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

package org.kiwix.kiwixmobile.core.page

import androidx.activity.compose.LocalActivity
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.calculateEndPadding
import androidx.compose.foundation.layout.calculateStartPadding
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalLayoutDirection
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.testTag
import androidx.compose.ui.text.TextStyle
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf
import org.kiwix.kiwixmobile.core.R
import org.kiwix.kiwixmobile.core.extensions.ActivityExtensions.isCustomApp
import org.kiwix.kiwixmobile.core.extensions.bottomShadow
import org.kiwix.kiwixmobile.core.extensions.hideKeyboardOnLazyColumnScroll
import org.kiwix.kiwixmobile.core.main.CoreMainActivity
import org.kiwix.kiwixmobile.core.page.DELETE_MENU_ICON_TESTING_TAG
import org.kiwix.kiwixmobile.core.page.SEARCH_ICON_TESTING_TAG
import org.kiwix.kiwixmobile.core.page.adapter.OnItemClickListener
import org.kiwix.kiwixmobile.core.page.adapter.Page
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem.DateItem
import org.kiwix.kiwixmobile.core.page.viewmodel.Action
import org.kiwix.kiwixmobile.core.page.viewmodel.PageState
import org.kiwix.kiwixmobile.core.page.viewmodel.PageViewModel
import org.kiwix.kiwixmobile.core.page.viewmodel.PageViewModelClickListener
import org.kiwix.kiwixmobile.core.ui.components.KiwixAppBar
import org.kiwix.kiwixmobile.core.ui.components.KiwixSearchView
import org.kiwix.kiwixmobile.core.ui.components.NavigationIcon
import org.kiwix.kiwixmobile.core.ui.models.ActionMenuItem
import org.kiwix.kiwixmobile.core.ui.models.IconItem
import org.kiwix.kiwixmobile.core.ui.theme.KiwixTheme
import org.kiwix.kiwixmobile.core.ui.theme.White
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.FOURTEEN_SP
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.KIWIX_TOOLBAR_SHADOW_ELEVATION
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.PAGE_SWITCH_LEFT_RIGHT_MARGIN
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.PAGE_SWITCH_ROW_BOTTOM_MARGIN
import org.kiwix.kiwixmobile.core.utils.ComposeDimens.SIXTEEN_DP
import org.kiwix.kiwixmobile.core.utils.dialog.AlertDialogShower
import org.threeten.bp.LocalDate
import org.threeten.bp.format.DateTimeFormatter
import org.threeten.bp.format.DateTimeParseException

const val SWITCH_TEXT_TESTING_TAG = "switchTextTestingTag"
const val NO_ITEMS_TEXT_TESTING_TAG = "noItemsTextTestingTag"
const val PAGE_LIST_TEST_TAG = "pageListTestingTag"

/**
 * A generic PageScreenRoute that works with any PageViewModel implementation.
 * This follows the pattern of HelpScreenRoute for full Compose navigation.
 *
 * @param navigateBack Callback to navigate back from this screen
 * @param pageViewModel The ViewModel that manages page data and actions
 * @param screenTitleRes String resource for the screen title
 * @param noItemsString String to display when there are no items
 * @param switchString String for the switch label
 * @param searchQueryHint String hint for the search field
 * @param deleteIconTitleRes String resource for delete icon content description
 * @param switchIsCheckedFlow Flow that indicates if the switch is checked
 * @param alertDialogShower AlertDialogShower for showing dialogs
 * @param pageViewModelClickListener Optional listener for item clicks. If null, uses default behavior.
 */
@Suppress("LongMethod", "LongParameterList")
@Composable
fun <T : Page, S : PageState<T>> PageScreenRoute(
  navigateBack: () -> Unit,
  pageViewModel: PageViewModel<T, S>,
  screenTitleRes: Int,
  noItemsString: String,
  switchString: String,
  searchQueryHint: String,
  deleteIconTitleRes: Int,
  switchIsCheckedFlow: Flow<Boolean>,
  alertDialogShower: AlertDialogShower,
  pageViewModelClickListener: PageViewModelClickListener? = null
) {
  val state by pageViewModel.state.collectAsStateWithLifecycle()
  val activity = LocalActivity.current as CoreMainActivity

  // Set up the alert dialog shower in the ViewModel
  LaunchedEffect(alertDialogShower) {
    pageViewModel.alertDialogShower = alertDialogShower
    pageViewModel.lifeCycleScope = activity.lifecycleScope
  }

  // Set up item click listener if provided
  LaunchedEffect(pageViewModelClickListener) {
    pageViewModelClickListener?.let {
      pageViewModel.setOnItemClickListener(it)
    }
  }

  var isSearchActive by remember { mutableStateOf(false) }
  var searchText by remember { mutableStateOf("") }

  PageScreen(
    state = state,
    screenTitleRes = screenTitleRes,
    noItemsString = noItemsString,
    switchString = switchString,
    searchQueryHint = searchQueryHint,
    deleteIconTitleRes = deleteIconTitleRes,
    switchIsCheckedFlow = switchIsCheckedFlow,
    isSearchActive = isSearchActive,
    searchText = searchText,
    onSearchTextChange = { newText ->
      searchText = newText
      pageViewModel.actions.tryEmit(Action.Filter(newText.trim()))
    },
    onClearSearch = {
      searchText = ""
      pageViewModel.actions.tryEmit(Action.Filter(""))
    },
    onSearchClick = { isSearchActive = true },
    onDeleteClick = { pageViewModel.actions.tryEmit(Action.UserClickedDeleteButton) },
    onSwitchCheckedChange = { isChecked ->
      pageViewModel.actions.tryEmit(Action.UserClickedShowAllToggle(isChecked))
    },
    onItemClick = { page ->
      if (state.isInSelectionState) {
        pageViewModel.actions.tryEmit(Action.OnItemLongClick(page))
      } else {
        pageViewModel.actions.tryEmit(Action.OnItemClick(page))
      }
    },
    onItemLongClick = { page ->
      pageViewModel.actions.tryEmit(Action.OnItemLongClick(page))
    },
    navigationIcon = {
      NavigationIcon(
        onClick = {
          if (isSearchActive) {
            isSearchActive = false
            searchText = ""
            pageViewModel.actions.tryEmit(Action.Filter(""))
          } else {
            navigateBack()
          }
        }
      )
    }
  )
}

@Suppress("ComposableLambdaParameterNaming", "LongMethod", "LongParameterList")
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun <T : Page, S : PageState<T>> PageScreen(
  state: S,
  screenTitleRes: Int,
  noItemsString: String,
  switchString: String,
  searchQueryHint: String,
  deleteIconTitleRes: Int,
  switchIsCheckedFlow: Flow<Boolean>,
  isSearchActive: Boolean,
  searchText: String,
  onSearchTextChange: (String) -> Unit,
  onClearSearch: () -> Unit,
  onSearchClick: () -> Unit,
  onDeleteClick: () -> Unit,
  onSwitchCheckedChange: (Boolean) -> Unit,
  onItemClick: (Page) -> Unit,
  onItemLongClick: (Page) -> Unit,
  navigationIcon: @Composable () -> Unit
) {
  KiwixTheme {
    Scaffold(
      topBar = {
        Column {
          KiwixAppBar(
            title = stringResource(screenTitleRes),
            navigationIcon = navigationIcon,
            actionMenuItems = actionMenuItems(
              isSearchActive = isSearchActive,
              onSearchClick = onSearchClick,
              onDeleteClick = onDeleteClick,
              deleteIconTitleRes = deleteIconTitleRes
            ),
            searchBar = if (isSearchActive) {
              {
                KiwixSearchView(
                  placeholder = searchQueryHint,
                  value = searchText,
                  searchViewTextFiledTestTag = "",
                  onValueChange = onSearchTextChange,
                  onClearClick = onClearSearch
                )
              }
            } else null
          )
          PageSwitchRow(
            switchString = switchString,
            switchIsCheckedFlow = switchIsCheckedFlow,
            switchIsEnabled = !state.isInSelectionState,
            onSwitchCheckedChange = onSwitchCheckedChange
          )
        }
      }
    ) { padding ->
      val items = state.pageItems
      Box(
        modifier = Modifier
          .padding(
            top = padding.calculateTopPadding(),
            start = padding.calculateStartPadding(LocalLayoutDirection.current),
            end = padding.calculateEndPadding(LocalLayoutDirection.current)
          )
          .fillMaxSize()
      ) {
        if (items.isEmpty()) {
          Text(
            text = noItemsString,
            style = MaterialTheme.typography.headlineSmall,
            modifier = Modifier
              .align(Alignment.Center)
              .semantics { testTag = NO_ITEMS_TEXT_TESTING_TAG }
          )
        } else {
          PageList(
            state = state,
            onItemClick = onItemClick,
            onItemLongClick = onItemLongClick
          )
        }
      }
    }
  }
}

/**
 * Backward-compatible overload for PageScreen that accepts PageFragmentScreenState.
 * This is used by the existing PageFragment.
 */
@Suppress(
  "ComposableLambdaParameterNaming",
  "LongMethod",
  "LongParameterList",
  "UnusedParameter"
)
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PageScreen(
  state: PageFragmentScreenState,
  itemClickListener: OnItemClickListener,
  actionMenuItems: List<ActionMenuItem>,
  navigationIcon: @Composable () -> Unit
) {
  PageScreen(
    state = state.pageState,
    screenTitleRes = state.screenTitle,
    noItemsString = state.noItemsString,
    switchString = state.switchString,
    searchQueryHint = state.searchQueryHint,
    deleteIconTitleRes = state.deleteIconTitle,
    switchIsCheckedFlow = state.switchIsCheckedFlow,
    isSearchActive = state.isSearchActive,
    searchText = state.searchText,
    onSearchTextChange = state.searchValueChangedListener,
    onClearSearch = state.clearSearchButtonClickListener,
    onSearchClick = { }, // handled by actionMenuItems
    onDeleteClick = { }, // handled by actionMenuItems
    onSwitchCheckedChange = state.onSwitchCheckedChanged,
    onItemClick = { itemClickListener.onItemClick(it) },
    onItemLongClick = { itemClickListener.onItemLongClick(it) },
    navigationIcon = navigationIcon
  )
}

@Composable
private fun actionMenuItems(
  isSearchActive: Boolean,
  onSearchClick: () -> Unit,
  onDeleteClick: () -> Unit,
  deleteIconTitleRes: Int
): List<ActionMenuItem> {
  return listOfNotNull(
    when {
      !isSearchActive -> ActionMenuItem(
        icon = IconItem.Drawable(R.drawable.action_search),
        contentDescription = R.string.search_label,
        onClick = onSearchClick,
        testingTag = SEARCH_ICON_TESTING_TAG
      )
      else -> null
    },
    ActionMenuItem(
      icon = IconItem.Vector(Icons.Default.Delete),
      contentDescription = deleteIconTitleRes,
      onClick = onDeleteClick,
      testingTag = DELETE_MENU_ICON_TESTING_TAG
    )
  )
}

@Composable
private fun <T : Page, S : PageState<T>> PageList(
  state: S,
  onItemClick: (Page) -> Unit,
  onItemLongClick: (Page) -> Unit
) {
  val listState = rememberLazyListState()
  LazyColumn(
    state = listState,
    modifier = Modifier
      .semantics { testTag = PAGE_LIST_TEST_TAG }
      // hides keyboard when scrolled
      .hideKeyboardOnLazyColumnScroll(listState)
  ) {
    itemsIndexed(state.visiblePageItems) { index, item ->
      when (item) {
        is Page -> PageListItem(
          index = index,
          page = item,
          onItemClick = onItemClick,
          onItemLongClick = onItemLongClick
        )
        is DateItem -> DateItemText(item)
      }
    }
  }
}

@Composable
fun PageSwitchRow(
  switchString: String,
  switchIsCheckedFlow: Flow<Boolean>,
  switchIsEnabled: Boolean,
  onSwitchCheckedChange: (Boolean) -> Unit
) {
  val context = LocalActivity.current as CoreMainActivity
  // hide switches for custom apps, see more info here https://github.com/kiwix/kiwix-android/issues/3523
  if (!context.isCustomApp()) {
    val isChecked by switchIsCheckedFlow.collectAsState(true)
    Surface(modifier = Modifier.bottomShadow(KIWIX_TOOLBAR_SHADOW_ELEVATION)) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .background(MaterialTheme.colorScheme.onPrimary)
          .padding(bottom = PAGE_SWITCH_ROW_BOTTOM_MARGIN),
        horizontalArrangement = Arrangement.Absolute.Right,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Text(
          switchString,
          color = MaterialTheme.colorScheme.onBackground,
          style = TextStyle(fontSize = FOURTEEN_SP),
          modifier = Modifier.testTag(SWITCH_TEXT_TESTING_TAG)
        )
        Switch(
          checked = isChecked,
          onCheckedChange = onSwitchCheckedChange,
          enabled = switchIsEnabled,
          modifier = Modifier
            .padding(horizontal = PAGE_SWITCH_LEFT_RIGHT_MARGIN),
          colors = SwitchDefaults.colors(
            uncheckedTrackColor = White
          )
        )
      }
    }
  }
}

@Composable
fun DateItemText(dateItem: DateItem) {
  Text(
    text = getFormattedDateLabel(dateItem.dateString),
    style = MaterialTheme.typography.bodySmall,
    modifier = Modifier.padding(SIXTEEN_DP)
  )
}

@Composable
private fun getFormattedDateLabel(dateString: String): String {
  val today = LocalDate.now()
  val yesterday = today.minusDays(1)

  val parsedDate = parseDateSafely(dateString)
  return when (parsedDate) {
    today -> stringResource(R.string.time_today)
    yesterday -> stringResource(R.string.time_yesterday)
    else -> dateString
  }
}

private fun parseDateSafely(dateString: String): LocalDate? {
  return try {
    LocalDate.parse(dateString, DateTimeFormatter.ofPattern("d MMM yyyy"))
  } catch (_: DateTimeParseException) {
    null
  }
}
