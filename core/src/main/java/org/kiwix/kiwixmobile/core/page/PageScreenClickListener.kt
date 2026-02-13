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

import org.kiwix.kiwixmobile.core.base.SideEffect
import org.kiwix.kiwixmobile.core.page.adapter.Page
import org.kiwix.kiwixmobile.core.page.viewmodel.PageViewModelClickListener

/**
 * Adapter class that wraps a lambda to implement PageViewModelClickListener.
 * This allows the ViewModel to use a simple lambda for handling item clicks.
 */
class PageScreenClickListener(
  private val onClick: (Page) -> SideEffect<*>
) : PageViewModelClickListener {
  override fun onItemClick(page: Page): SideEffect<*> = onClick(page)
}
