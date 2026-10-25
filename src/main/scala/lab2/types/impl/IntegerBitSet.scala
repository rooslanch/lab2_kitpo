package lab2.types.impl

import java.io.InputStreamReader
import java.util.BitSet

import lab2.types.{Comparator, UserType}

import scala.collection.mutable.StringBuilder

/**
 * Множество целых неотрицательных чисел, представленное битовым массивом (вариант 10).
 * i-й разряд 1/0 — наличие/отсутствие числа i в множестве.
 * Операции: объединение, пересечение, разность, добавление, проверка вхождения.
 * Сравнение — по мощности (cardinality).
 */
class IntegerBitSet(rawBits: BitSet = new BitSet()) extends UserType[IntegerBitSet] {

  /** Внутреннее битовое множество. Конструктор клонирует переданное — безопасная копия. */
  private val bits: BitSet = rawBits.clone().asInstanceOf[BitSet]

  // ==================== Операции множества ====================

  /** Добавляет элемент в множество. */
  def add(value: Int): Unit = {
    require(value >= 0, s"Отрицательные числа не поддерживаются: $value")
    bits.set(value)
  }

  /** Проверяет, входит ли число в множество. */
  def contains(value: Int): Boolean = {
    require(value >= 0, s"Отрицательные числа не поддерживаются: $value")
    bits.get(value)
  }

  /** Объединение: this = this ∪ other. */
  def union(other: IntegerBitSet): Unit = bits.or(other.bits)

  /** Пересечение: this = this ∩ other. */
  def intersection(other: IntegerBitSet): Unit = bits.and(other.bits)

  /** Разность: this = this \ other. */
  def difference(other: IntegerBitSet): Unit = bits.andNot(other.bits)

  /** Мощность множества (количество элементов). */
  def cardinality(): Int = bits.cardinality()

  // ==================== Реализация UserType ====================

  override def typeName(): String = "IntegerBitSet"

  override def create(): IntegerBitSet = new IntegerBitSet()

  override def copy(): IntegerBitSet = new IntegerBitSet(bits)

  override def readValue(in: InputStreamReader): IntegerBitSet = {
    val sb = new StringBuilder()
    var c = in.read()
    while (c != -1 && c != '\n') {
      if (c != '\r') sb.append(c.toChar)
      c = in.read()
    }
    parseValue(sb.toString())
  }

  /**
   * Парсит строку вида "{1, 3, 5}", "1, 3, 5", "1 3 5" или "{}".
   * Разделители — запятые и/или пробелы. Фигурные скобки необязательны.
   */
  override def parseValue(ss: String): IntegerBitSet = {
    val result = new IntegerBitSet()
    if (ss == null || ss.isBlank) return result
    var cleaned = ss.trim
    if (cleaned.startsWith("{")) cleaned = cleaned.substring(1)
    if (cleaned.endsWith("}")) cleaned = cleaned.substring(0, cleaned.length - 1)
    if (cleaned.isBlank) return result
    cleaned.split("[,\\s]+").foreach { part =>
      if (!part.isBlank) result.add(part.trim.toInt)
    }
    result
  }

  /** Компаратор — по мощности множества (cardinality). */
  override def getTypeComparator(): Comparator[IntegerBitSet] =
    (o1, o2) => Integer.compare(o1.cardinality(), o2.cardinality())

  // ==================== Отображение ====================

  /** Формат: "{1, 3, 5}" или "{}" для пустого. Используется и для сериализации. */
  override def toString(): String = {
    val sb = new StringBuilder("{")
    var first = true
    var i = bits.nextSetBit(0)
    while (i >= 0) {
      if (!first) sb.append(", ")
      sb.append(i)
      first = false
      i = bits.nextSetBit(i + 1)
    }
    sb.append("}")
    sb.toString()
  }

  override def equals(o: Any): Boolean = o match {
    case that: IntegerBitSet => bits.equals(that.bits)
    case _                   => false
  }

  override def hashCode(): Int = bits.hashCode()
}
