/*
 * Kiwix Android
 * Copyright (c) 2024 Kiwix <android.kiwix.org>
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
package org.kiwix.kiwixmobile.webserver

import kotlinx.coroutines.flow.first
import org.kiwix.kiwixmobile.core.base.BasePresenter
import org.kiwix.kiwixmobile.core.data.DataSource
import org.kiwix.kiwixmobile.core.di.ActivityScope
import org.kiwix.kiwixmobile.core.utils.files.Log
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem
import org.kiwix.kiwixmobile.webserver.ZimHostContract.Presenter
import org.kiwix.kiwixmobile.webserver.ZimHostContract.View
import javax.inject.Inject

@ActivityScope
class ZimHostPresenter @Inject internal constructor(
  private val dataSource: DataSource
) : BasePresenter<View>(),
  Presenter {
  @Suppress("TooGenericExceptionCaught")
  override suspend fun loadBooks(previouslyHostedBooks: Set<String>) {
    try {
      val books = dataSource.getLanguageCategorizedBooks().first()
      books.forEach { item ->
        if (item is BooksOnDiskListItem.BookOnDisk) {
          item.isSelected =
            previouslyHostedBooks.contains(item.book.title) || previouslyHostedBooks.isEmpty()
        }
      }
      view?.addBooks(books)
    } catch (e: Exception) {
      Log.e(TAG, "Unable to load books", e)
    }
  }

  companion object {
    private const val TAG = "ZimHostPresenter"
  }
}
