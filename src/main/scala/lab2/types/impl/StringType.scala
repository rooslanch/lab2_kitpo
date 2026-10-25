package lab2.types.impl

import java.io.InputStreamReader

import lab2.types.{Comparator, UserType}

import scala.collection.mutable.StringBuilder

/**
 * Обёртка над строкой — простейший хранимый тип для демонстрации
 * работы структуры данных с произвольным типом.
 * Сравнение — лексикографическое (String.compareTo).
 */
class StringType(private var value: String = "") extends UserType[StringType] {

  if (value == null) value = ""

  // ==================== Операции ====================

  def getValue(): String = value

  def setValue(v: String): Unit = { value = if (v != null) v else "" }

  // ==================== Реализация UserType ====================

  override def typeName(): String = "String"

  override def create(): StringType = new StringType("")

  override def copy(): StringType = new StringType(value)

  override def readValue(in: InputStreamReader): StringType = {
    val sb = new StringBuilder()
    var c = in.read()
    while (c != -1 && c != '\n') {
      if (c != '\r') sb.append(c.toChar)
      c = in.read()
    }
    parseValue(sb.toString())
  }

  /** Строка берётся как есть — без разбора форматов. */
  override def parseValue(ss: String): StringType = new StringType(ss)

  /** Компаратор — лексикографическое сравнение строк. */
  override def getTypeComparator(): Comparator[StringType] =
    (o1, o2) => o1.value.compareTo(o2.value)

  // ==================== Отображение ====================

  /** Возвращает содержимое строки как есть. */
  override def toString(): String = value

  override def equals(o: Any): Boolean = o match {
    case that: StringType => value == that.value
    case _                => false
  }

  override def hashCode(): Int = value.hashCode()
}
