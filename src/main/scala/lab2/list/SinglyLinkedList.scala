package lab2.list

import lab2.types.Comparator

object SinglyLinkedList {
  /** Узел односвязного списка: данные + ссылка на следующий узел. */
  case class Node[A](var data: A, var next: Option[Node[A]] = None)
}

/**
 * Односвязный список, хранящий элементы произвольного типа.
 * Элементы доступны только последовательно.
 * В отличие от lab1 (raw Object), использует generic-параметр A.
 * Итераторы реализованы через функции — требование lab2.
 */
class SinglyLinkedList[A] {
  import SinglyLinkedList.Node

  private var head: Option[Node[A]] = None
  private var tail: Option[Node[A]] = None
  private var count: Int = 0

  // ==================== Базовые операции ====================

  /** Добавляет элемент в конец списка. O(1) — за счёт хвостового указателя. */
  def addToEnd(value: A): Unit = {
    val node = Node(value)
    head match {
      case None =>
        head = Some(node); tail = Some(node)
      case Some(_) =>
        tail.get.next = Some(node); tail = Some(node)
    }
    count += 1
  }

  /** Возвращает элемент по логическому индексу (0 — первый). */
  def get(index: Int): A = {
    checkIndex(index)
    var current = head.get
    for (_ <- 0 until index) current = current.next.get
    current.data
  }

  /** Вставляет элемент по индексу (0 — перед первым, size — в конец). */
  def insert(index: Int, value: A): Unit = {
    if (index < 0 || index > count)
      throw new IndexOutOfBoundsException(s"Индекс $index вне [0, $count]")
    if (index == count) { addToEnd(value); return }
    val node = Node(value)
    if (index == 0) {
      node.next = head
      head = Some(node)
    } else {
      var prev = head.get
      for (_ <- 0 until index - 1) prev = prev.next.get
      node.next = prev.next
      prev.next = Some(node)
    }
    count += 1
  }

  /** Удаляет элемент по индексу и возвращает его. */
  def remove(index: Int): A = {
    checkIndex(index)
    val removed: A = if (index == 0) {
      val d = head.get.data
      head = head.get.next
      if (head.isEmpty) tail = None
      d
    } else {
      var prev = head.get
      for (_ <- 0 until index - 1) prev = prev.next.get
      val d = prev.next.get.data
      prev.next = prev.next.get.next
      if (prev.next.isEmpty) tail = Some(prev)
      d
    }
    count -= 1
    removed
  }
  // ==================== Итерация через функции ====================

  /** Применяет функцию к каждому элементу (требование lab2: параметр — функция). */
  def forEach(f: A => Unit): Unit = {
    var current = head
    while (current.isDefined) { f(current.get.data); current = current.get.next }
  }

  /** Возвращает первый элемент, удовлетворяющий предикату (Option вместо nullable). */
  def firstThat(f: A => Boolean): Option[A] = {
    var current = head
    while (current.isDefined) {
      if (f(current.get.data)) return Some(current.get.data)
      current = current.get.next
    }
    None
  }

  // ==================== Сортировка ====================

  /**
   * Императивная сортировка слиянием (merge sort) за O(n log n).
   * Переставляет ссылки next между существующими узлами.
   * Мутирует текущий список (in-place).
   */
  def sortImperative(comp: Comparator[A]): Unit = {
    if (head.isEmpty || head.get.next.isEmpty) return
    head = mergeSortNodes(head, comp)
    tail = head
    while (tail.isDefined && tail.get.next.isDefined) tail = tail.get.next
  }

  /**
   * Функциональная сортировка слиянием (merge sort).
   * Рекурсивное разделение + слияние с созданием новых узлов.
   * Возвращает новый список; текущий не изменяется.
   */
  def sortFunctional(comp: Comparator[A]): SinglyLinkedList[A] = {
    val sorted = functionalMergeSort(toList, comp)
    val result = new SinglyLinkedList[A]
    sorted.foreach(result.addToEnd)
    result
  }

  // ==================== Вспомогательные ====================

  /** Возвращает элементы в виде иммутабельного Scala List. */
  def toList: List[A] = {
    val buf = scala.collection.mutable.ListBuffer[A]()
    forEach(buf += _)
    buf.toList
  }

  def size: Int = count

  def isEmpty: Boolean = count == 0

  override def toString(): String = {
    val sb = new StringBuilder("[")
    var current = head
    while (current.isDefined) {
      sb.append(current.get.data)
      if (current.get.next.isDefined) sb.append(", ")
      current = current.get.next
    }
    sb.append("]")
    sb.toString()
  }

  // ==================== Служебное ====================

  private def checkIndex(index: Int): Unit = {
    if (index < 0 || index >= count)
      throw new IndexOutOfBoundsException(s"Индекс $index вне [0, $count)")
  }

  // --- Императивный merge sort ---

  private def mergeSortNodes(nodeOpt: Option[Node[A]], comp: Comparator[A]): Option[Node[A]] = {
    if (nodeOpt.isEmpty || nodeOpt.get.next.isEmpty) return nodeOpt
    var slow = nodeOpt
    var fast = nodeOpt.get.next
    while (fast.isDefined && fast.get.next.isDefined) {
      slow = slow.get.next
      fast = fast.get.next.get.next
    }
    val middle = slow.get
    val rightStart = middle.next
    middle.next = None
    val leftSorted = mergeSortNodes(nodeOpt, comp)
    val rightSorted = mergeSortNodes(rightStart, comp)
    mergeNodes(leftSorted, rightSorted, comp)
  }

  private def mergeNodes(leftOpt: Option[Node[A]], rightOpt: Option[Node[A]], comp: Comparator[A]): Option[Node[A]] = {
    val dummy = Node[A](null.asInstanceOf[A])
    var current = dummy
    var left = leftOpt
    var right = rightOpt
    while (left.isDefined && right.isDefined) {
      if (comp(left.get.data, right.get.data) <= 0) {
        current.next = left; left = left.get.next
      } else {
        current.next = right; right = right.get.next
      }
      current = current.next.get
    }
    current.next = if (left.isDefined) left else right
    dummy.next
  }

  // --- Функциональный merge sort ---

  private def functionalMergeSort(list: List[A], comp: Comparator[A]): List[A] = list match {
    case Nil | _ :: Nil => list
    case _ =>
      val (l, r) = list.splitAt(list.size / 2)
      mergeLists(functionalMergeSort(l, comp), functionalMergeSort(r, comp), comp)
  }

  private def mergeLists(left: List[A], right: List[A], comp: Comparator[A]): List[A] = (left, right) match {
    case (Nil, r) => r
    case (l, Nil) => l
    case (lh :: lt, rh :: rt) =>
      if (comp(lh, rh) <= 0) lh :: mergeLists(lt, right, comp)
      else rh :: mergeLists(left, rt, comp)
  }
}
