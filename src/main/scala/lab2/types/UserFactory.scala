package lab2.types

import lab2.types.impl.{IntegerBitSet, StringType}

import scala.collection.mutable

/**
 * Фабрика хранимых типов данных (Layer 1).
 * Хранит зарегистрированные прототипы [[UserType]] по имени типа.
 * Для добавления нового ТД достаточно создать класс, реализующий [[UserType]],
 * и добавить его экземпляр через [[register]].
 * Никакие другие изменения в код не требуются.
 */
object UserFactory {

  private val prototypes: mutable.LinkedHashMap[String, UserType[?]] =
    mutable.LinkedHashMap.empty

  register(new IntegerBitSet())
  register(new StringType())

  /** Регистрирует прототип нового хранимого типа. */
  def register(t: UserType[?]): Unit = {
    prototypes.put(t.typeName(), t)
  }

  /** Возвращает список имён зарегистрированных типов (для выпадающего списка в GUI). */
  def getTypeNameList(): List[String] = prototypes.keys.toList

  /** Возвращает прототип-строитель по имени типа. */
  def getBuilderByName(name: String): Option[UserType[?]] = prototypes.get(name)
}
