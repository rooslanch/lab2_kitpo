package lab2.types

import java.io.InputStreamReader

/**
 * Прототип хранимого типа данных (Layer 1).
 * В отличие от lab1 (interface UserType, работавший с Object),
 * здесь используется generic-параметр T для типобезопасности.
 */
trait UserType[T <: AnyRef] {

  /** Имя типа — ключ в фабрике и в сериализации. */
  def typeName(): String

  /** Создаёт новый пустой экземпляр. */
  def create(): T

  /** Создаёт копию текущего экземпляра. (В lab1 называлось clone(); в Scala переименовано — конфликт с protected Object.clone().) */
  def copy(): T

  /**
   * Читает одно значение из потока (посимвольно до '\n').
   * Посимвольное чтение — чтобы не «съесть» данные следующих строк
   * при повторных вызовах на одном потоке.
   */
  def readValue(in: InputStreamReader): T

  /** Парсит строковое представление значения. */
  def parseValue(ss: String): T

  /** Возвращает компаратор для этого типа — внешнюю функцию (A, A) => Int. */
  def getTypeComparator(): Comparator[T]
}
