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
package org.kiwix.kiwixmobile.core.dao

import io.objectbox.Box
import io.objectbox.kotlin.flow
import io.objectbox.kotlin.query
import io.objectbox.query.Query
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import org.kiwix.kiwixmobile.core.dao.entities.LanguageEntity
import org.kiwix.kiwixmobile.core.zim_manager.Language
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class NewLanguagesDao @Inject constructor(private val box: Box<LanguageEntity>) {
  fun languages() =
    box.asFlow()
      .map { it.map(LanguageEntity::toLanguageModel) }

  fun insert(languages: List<Language>) {
    box.store.callInTx {
      box.removeAll()
      box.put(languages.map(::LanguageEntity))
    }
  }
}

@OptIn(ExperimentalCoroutinesApi::class)
fun <T> Box<T>.asFlow(query: Query<T> = query {}): Flow<List<T>> {
  return query.flow()
    .map { it.toList() }
    .distinctUntilChanged()
}
