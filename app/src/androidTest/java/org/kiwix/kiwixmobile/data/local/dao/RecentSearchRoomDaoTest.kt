/*
 * Kiwix Android
 * Copyright (c) 2022 Kiwix <android.kiwix.org>
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

package org.kiwix.kiwixmobile.data.local.dao

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.count
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.Assertions
import org.junit.jupiter.api.Test
import org.junit.runner.RunWith
import org.kiwix.kiwixmobile.core.dao.RecentSearchRoomDao
import org.kiwix.kiwixmobile.core.data.local.KiwixRoomDatabase
import java.io.IOException

@RunWith(AndroidJUnit4::class)
class RecentSearchRoomDaoTest {

  private lateinit var recentSearchRoomDao: RecentSearchRoomDao
  private lateinit var db: KiwixRoomDatabase

  @Test
  @Throws(IOException::class)
  fun testFullSearch() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    recentSearchRoomDao.fullSearch().collect {
      Assertions.assertEquals(2, it.size)
    }
  }

  @Test
  @Throws(IOException::class)
  fun testSearch() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val searchTerm3 = "title3"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    Assertions.assertEquals(1, recentSearchRoomDao.search(zimId).count())
    Assertions.assertEquals(1, recentSearchRoomDao.search(zimId2).count())
    recentSearchRoomDao.saveSearch(searchTerm3, zimId)
    Assertions.assertEquals(2, recentSearchRoomDao.search(zimId).count())
    Assertions.assertEquals(1, recentSearchRoomDao.search(zimId2).count())
  }

  @Test
  @Throws(IOException::class)
  fun testRecentSearches() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val searchTerm3 = "title3"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    recentSearchRoomDao.saveSearch(searchTerm3, zimId)
    Assertions.assertEquals(searchTerm3, recentSearchRoomDao.recentSearches(zimId).first())
  }

  @Test
  @Throws(IOException::class)
  fun testDeleteSearch() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val searchTerm3 = "title3"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    recentSearchRoomDao.saveSearch(searchTerm3, zimId)
    recentSearchRoomDao.deleteSearchString(searchTerm)
    Assertions.assertEquals(1, recentSearchRoomDao.search(zimId).count())
    Assertions.assertEquals(1, recentSearchRoomDao.search(zimId2).count())
  }

  @Test
  @Throws(IOException::class)
  fun testDeleteAllSearch() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val searchTerm3 = "title3"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    recentSearchRoomDao.saveSearch(searchTerm3, zimId)
    recentSearchRoomDao.deleteSearchString(searchTerm)
    recentSearchRoomDao.deleteSearchString(searchTerm2)
    recentSearchRoomDao.deleteSearchString(searchTerm3)
    Assertions.assertEquals(0, recentSearchRoomDao.search(zimId).count())
    Assertions.assertEquals(0, recentSearchRoomDao.search(zimId2).count())
  }

  @Test
  @Throws(IOException::class)
  fun testDeleteAllTheTable() = runBlocking {
    val context = ApplicationProvider.getApplicationContext<Context>()
    db = Room.inMemoryDatabaseBuilder(
      context, KiwixRoomDatabase::class.java
    ).build()
    recentSearchRoomDao = db.recentSearchRoomDao()
    val searchTerm = "title"
    val searchTerm2 = "title2"
    val searchTerm3 = "title3"
    val zimId = "zimId"
    val zimId2 = "zimId2"
    recentSearchRoomDao.saveSearch(searchTerm, zimId)
    recentSearchRoomDao.saveSearch(searchTerm2, zimId2)
    recentSearchRoomDao.saveSearch(searchTerm3, zimId)
    recentSearchRoomDao.deleteSearchHistory()
    Assertions.assertEquals(0, recentSearchRoomDao.fullSearch().count())
    Assertions.assertEquals(0, recentSearchRoomDao.search(zimId).count())
    Assertions.assertEquals(0, recentSearchRoomDao.search(zimId2).count())
  }
}