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
package org.kiwix.kiwixmobile.core.data

import io.reactivex.Completable
import kotlinx.coroutines.flow.Flow
import org.kiwix.kiwixmobile.core.dao.entities.WebViewHistoryEntity
import org.kiwix.kiwixmobile.core.page.bookmark.adapter.LibkiwixBookmarkItem
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem.HistoryItem
import org.kiwix.kiwixmobile.core.page.notes.adapter.NoteListItem
import org.kiwix.kiwixmobile.core.zim_manager.Language
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem.BookOnDisk

/**
 * Defines the set of methods which are required to provide the presenter with the requisite data.
 */
interface DataSource {
  fun getLanguageCategorizedBooks(): Flow<List<BooksOnDiskListItem>>
  fun saveBook(book: BookOnDisk): Completable
  fun saveBooks(book: List<BookOnDisk>): Completable
  fun saveLanguages(languages: List<Language>): Completable
  fun saveHistory(history: HistoryItem): Completable
  fun deleteHistory(historyList: List<HistoryListItem>): Completable
  fun clearHistory(): Completable
  fun getBookmarks(): Flow<List<LibkiwixBookmarkItem>>
  suspend fun getCurrentZimBookmarksUrl(): List<String>
  suspend fun saveBookmark(libkiwixBookmarkItem: LibkiwixBookmarkItem)
  suspend fun deleteBookmarks(bookmarks: List<LibkiwixBookmarkItem>)
  suspend fun deleteBookmark(bookId: String, bookmarkUrl: String)
  fun booksOnDiskAsListItems(): Flow<List<BooksOnDiskListItem>>
  fun saveNote(noteListItem: NoteListItem): Completable
  fun deleteNote(noteTitle: String): Completable
  fun deleteNotes(noteList: List<NoteListItem>): Completable
  suspend fun insertWebViewPageHistoryItems(webViewHistoryEntityList: List<WebViewHistoryEntity>)
  fun getAllWebViewPagesHistory(): Flow<List<WebViewHistoryEntity>>
  suspend fun clearWebViewPagesHistory()
}
