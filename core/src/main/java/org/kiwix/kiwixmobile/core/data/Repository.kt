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
import io.reactivex.Scheduler
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.map
import org.kiwix.kiwixmobile.core.dao.HistoryRoomDao
import org.kiwix.kiwixmobile.core.dao.LibkiwixBookmarks
import org.kiwix.kiwixmobile.core.dao.NewBookDao
import org.kiwix.kiwixmobile.core.dao.NewLanguagesDao
import org.kiwix.kiwixmobile.core.dao.NotesRoomDao
import org.kiwix.kiwixmobile.core.dao.RecentSearchRoomDao
import org.kiwix.kiwixmobile.core.dao.WebViewHistoryRoomDao
import org.kiwix.kiwixmobile.core.dao.entities.WebViewHistoryEntity
import org.kiwix.kiwixmobile.core.di.qualifiers.IO
import org.kiwix.kiwixmobile.core.extensions.HeaderizableList
import org.kiwix.kiwixmobile.core.page.bookmark.adapter.LibkiwixBookmarkItem
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem
import org.kiwix.kiwixmobile.core.page.history.adapter.HistoryListItem.HistoryItem
import org.kiwix.kiwixmobile.core.page.notes.adapter.NoteListItem
import org.kiwix.kiwixmobile.core.reader.ZimReaderContainer
import org.kiwix.kiwixmobile.core.zim_manager.Language
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem.BookOnDisk
import org.kiwix.kiwixmobile.core.zim_manager.fileselect_view.BooksOnDiskListItem.LanguageItem
import javax.inject.Inject
import javax.inject.Singleton

/**
 * A central repository of data which should provide the presenters with the required data.
 */

@Singleton
class Repository @Inject internal constructor(
  @param:IO private val ioThread: Scheduler,
  private val bookDao: NewBookDao,
  private val libkiwixBookmarks: LibkiwixBookmarks,
  private val historyRoomDao: HistoryRoomDao,
  private val webViewHistoryRoomDao: WebViewHistoryRoomDao,
  private val notesRoomDao: NotesRoomDao,
  private val languageDao: NewLanguagesDao,
  private val recentSearchRoomDao: RecentSearchRoomDao,
  private val zimReaderContainer: ZimReaderContainer
) : DataSource {
  override fun getLanguageCategorizedBooks() =
    booksOnDiskAsListItems()
      .map { it.ifEmpty { emptyList() } }

  @Suppress("InjectDispatcher")
  override fun booksOnDiskAsListItems(): Flow<List<BooksOnDiskListItem>> =
    bookDao.books()
      .map { books ->
        books.flatMap { bookOnDisk ->
          // Split languages if there are multiple, otherwise return the single book. Bug fix #3892
          if (bookOnDisk.book.language.contains(',')) {
            bookOnDisk.book.language.split(',').map { lang ->
              bookOnDisk.copy(book = bookOnDisk.book.copy(language = lang.trim()))
            }
          } else {
            listOf(bookOnDisk)
          }
        }.distinctBy { it.book.language to it.book.title }
          .sortedBy { it.book.language + it.book.title }
      }
      .map { items ->
        HeaderizableList<BooksOnDiskListItem, BookOnDisk, LanguageItem>(items).foldOverAddingHeaders(
          { bookOnDisk -> LanguageItem(bookOnDisk.locale) },
          { current, next -> current.locale.displayName != next.locale.displayName }
        )
      }
      .map(MutableList<BooksOnDiskListItem>::toList)
      .flowOn(Dispatchers.IO)

  override fun saveBooks(books: List<BookOnDisk>) =
    Completable.fromAction { bookDao.insert(books) }
      .subscribeOn(ioThread)

  override fun saveBook(book: BookOnDisk) =
    Completable.fromAction { bookDao.insert(listOf(book)) }
      .subscribeOn(ioThread)

  override fun saveLanguages(languages: List<Language>) =
    Completable.fromAction { languageDao.insert(languages) }
      .subscribeOn(ioThread)

  override fun saveHistory(history: HistoryItem) =
    Completable.fromAction { historyRoomDao.saveHistory(history) }
      .subscribeOn(ioThread)

  override fun deleteHistory(historyList: List<HistoryListItem>) =
    Completable.fromAction {
      historyRoomDao.deleteHistory(historyList.filterIsInstance(HistoryItem::class.java))
    }
      .subscribeOn(ioThread)

  override fun clearHistory() =
    Completable.fromAction {
      historyRoomDao.deleteAllHistory()
      recentSearchRoomDao.deleteSearchHistory()
    }.subscribeOn(ioThread)

  override fun getBookmarks() =
    libkiwixBookmarks.bookmarks() as Flow<List<LibkiwixBookmarkItem>>

  override suspend fun getCurrentZimBookmarksUrl() =
    libkiwixBookmarks.getCurrentZimBookmarksUrl(zimReaderContainer.zimFileReader)

  override suspend fun saveBookmark(libkiwixBookmarkItem: LibkiwixBookmarkItem) =
    libkiwixBookmarks.saveBookmark(libkiwixBookmarkItem)

  override suspend fun deleteBookmarks(bookmarks: List<LibkiwixBookmarkItem>) =
    libkiwixBookmarks.deleteBookmarks(bookmarks)

  override suspend fun deleteBookmark(bookId: String, bookmarkUrl: String) =
    libkiwixBookmarks.deleteBookmark(bookId, bookmarkUrl)

  override fun saveNote(noteListItem: NoteListItem): Completable =
    Completable.fromAction { notesRoomDao.saveNote(noteListItem) }
      .subscribeOn(ioThread)

  override fun deleteNotes(noteList: List<NoteListItem>) =
    Completable.fromAction { notesRoomDao.deleteNotes(noteList) }
      .subscribeOn(ioThread)

  override suspend fun insertWebViewPageHistoryItems(
    webViewHistoryEntityList: List<WebViewHistoryEntity>
  ) {
    webViewHistoryRoomDao.insertWebViewPageHistoryItems(webViewHistoryEntityList)
  }

  override fun getAllWebViewPagesHistory() =
    webViewHistoryRoomDao.getAllWebViewPagesHistory()

  override suspend fun clearWebViewPagesHistory() {
    webViewHistoryRoomDao.clearWebViewPagesHistory()
  }

  override fun deleteNote(noteTitle: String): Completable =
    Completable.fromAction { notesRoomDao.deleteNote(noteTitle) }
      .subscribeOn(ioThread)
}
