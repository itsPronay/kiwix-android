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

package org.kiwix.kiwixmobile.core.dao.entities

import io.objectbox.annotation.Convert
import io.objectbox.annotation.Entity
import io.objectbox.annotation.Id
import io.objectbox.converter.PropertyConverter
import org.kiwix.kiwixmobile.core.compat.CompatHelper.Companion.convertToLocal
import org.kiwix.kiwixmobile.core.zim_manager.Language
import java.util.Locale

@Entity
data class LanguageEntity(
  @Id var id: Long = 0,
  @Convert(converter = StringToLocaleConverter::class, dbType = String::class)
  val locale: Locale = Locale.ENGLISH,
  val active: Boolean = false,
  val occurencesOfLanguage: Int = 0
) {

  constructor(language: Language) : this(
    0,
    language.languageCode.convertToLocal(),
    language.active,
    language.occurencesOfLanguage
  )

  fun toLanguageModel() =
    Language(locale, active, occurencesOfLanguage, id)
}

class StringToLocaleConverter : PropertyConverter<Locale, String> {
  override fun convertToDatabaseValue(entityProperty: Locale?): String =
    entityProperty?.isO3Language ?: Locale.ENGLISH.isO3Language

  override fun convertToEntityProperty(databaseValue: String?): Locale =
    databaseValue?.convertToLocal() ?: Locale.ENGLISH
}
