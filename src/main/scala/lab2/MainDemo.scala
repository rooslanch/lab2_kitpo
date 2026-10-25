package lab2

import lab2.list.SinglyLinkedList
import lab2.persist.TextSerializer
import lab2.types.{Comparator, UserFactory, UserType}
import lab2.types.impl.IntegerBitSet

import java.nio.file.Path

/**
 * Консольная демонстрация работы структуры данных.
 * Один и тот же сценарий операций прогоняется для каждого
 * хранимого типа, взятого из [[UserFactory]].
 */
object MainDemo {

  /**
   * Единый сценарий тестирования для произвольного типа.
   * addToEnd → insert → get → remove → forEach → обе сортировки → firstThat → save → load → toString
   */
  def testType(proto: UserType[?]): Unit = {
    val serializer = new TextSerializer[AnyRef]()
    val list = new SinglyLinkedList[AnyRef]()
    val initial = getInitial(proto)
    val insertHead = getInsertHead(proto)
    val insertMid = getInsertMid(proto)

    // 1. addToEnd
    println(s"\n[1] addToEnd x ${initial.length}")
    for s <- initial do
      list.addToEnd(proto.parseValue(s))
    printList(list)

    // 2. insert
    println(s"\n[2] insert(0, $insertHead)")
    list.insert(0, proto.parseValue(insertHead))
    printList(list)

    println(s"\n[2] insert(2, $insertMid)")
    list.insert(2, proto.parseValue(insertMid))
    printList(list)

    // 3. get
    println(s"\n[3] get(0) = ${list.get(0)}")
    println(s"[3] get(2) = ${list.get(2)}")
    println(s"[3] get(последний) = ${list.get(list.size - 1)}")

    // 4. remove
    println("\n[4] remove(0)")
    println(s"    удалён: ${list.remove(0)}")
    printList(list)

    // 5. forEach
    println("\n[5] forEach:")
    list.forEach(obj => println(s"    -> $obj"))

    // 6. Сортировки — обе на одном наборе данных
    val comp = proto.getTypeComparator().asInstanceOf[Comparator[AnyRef]]

    println("\n[6a] sortImperative — сортировка на месте (мутирует список):")
    list.sortImperative(comp)
    printList(list)

    println("\n[6b] sortFunctional — возвращает НОВЫЙ отсортированный список:")
    val sortedNew = list.sortFunctional(comp)
    println(s"    оригинал (после imperative): $list")
    println(s"    новый (результат functional): $sortedNew")

    // 7. firstThat
    println("\n[7] firstThat(toString().length() > 5):")
    val found = list.firstThat(obj => obj.toString.length > 5)
    println(s"    найдено: ${found.getOrElse("<ничего>")}")

    // 8. save (TextSerializer)
    val file = Path.of(s"demo_${proto.typeName()}.txt")
    println(s"\n[8] save -> ${file.toAbsolutePath}")
    val saved = try {
      serializer.save(proto, list, file)
      println("    OK")
      true
    } catch {
      case e: Exception =>
        println(s"    ОШИБКА: ${e.getMessage}")
        false
    }

    if saved then {
      // 9. load (TextSerializer)
      println(s"\n[9] load <- $file")
      try {
        val loaded = serializer.load(file)
        println(s"    загружено ${loaded.size} элементов:")
        loaded.forEach(obj => println(s"    -> $obj"))
      } catch {
        case e: Exception =>
          println(s"    ОШИБКА: ${e.getMessage}")
      }

      // 10. Итоговый toString
      println(s"\n[10] итоговый toString: $list")
    }
  }

  /**
   * Демонстрация операций множества (вариант ТД: IntegerBitSet).
   * Это операции уровня ТД — вызываются на самих элементах,
   * а не на списке; СД об общих операциях множеств не знает.
   * Операции union/intersection/difference мутируют объект-получатель
   * (this = this ∪ other), поэтому для каждого вычисления берётся copy().
   */
  def testSetOperations(): Unit = {
    println("\n============================================")
    println("  Операции множества (IntegerBitSet)")
    println("============================================")

    val a = new IntegerBitSet().parseValue("{1, 5, 10}")
    val b = new IntegerBitSet().parseValue("{5, 10, 20}")

    println(s"\nA = $a   (мощность ${a.cardinality()})")
    println(s"B = $b   (мощность ${b.cardinality()})")

    println("\n[1] union: A ∪ B")
    val u = a.copy()
    u.union(b)
    println(s"    $a ∪ $b = $u")

    println("\n[2] intersection: A ∩ B")
    val i = a.copy()
    i.intersection(b)
    println(s"    $a ∩ $b = $i")

    println("\n[3] difference: A \\ B")
    val d = a.copy()
    d.difference(b)
    println(s"    $a \\ $b = $d")

    println("\n[4] add / contains:")
    println(s"    A.contains(5)  = ${a.contains(5)}")
    println(s"    A.contains(7)  = ${a.contains(7)}")
    a.add(7)
    println(s"    A.add(7) -> A = $a")

    println("\n[5] состояние после операций:")
    println(s"    A = $a   (мощность ${a.cardinality()})")
    println(s"    B = $b   (мощность ${b.cardinality()})")
  }

  // ==================== Вспомогательные ====================

  /** Печать размера и содержимого списка. */
  private def printList(list: SinglyLinkedList[AnyRef]): Unit =
    println(s"    (${list.size}) $list")

  // ====== Тестовые данные по типам ======

  private def getInitial(proto: UserType[?]): Array[String] =
    proto.typeName() match
      case "IntegerBitSet" => Array("{1, 5, 10}", "{3, 7}", "{2}", "{0, 4, 8, 12}", "{6}")
      case "String"        => Array("banana", "apple", "cherry", "date", "elderberry")
      case _               => Array("a", "b", "c", "d", "e")

  private def getInsertHead(proto: UserType[?]): String =
    proto.typeName() match
      case "IntegerBitSet" => "{99}"
      case "String"        => "zzz"
      case _               => "x"

  private def getInsertMid(proto: UserType[?]): String =
    proto.typeName() match
      case "IntegerBitSet" => "{42}"
      case "String"        => "mmm"
      case _               => "y"

  /** Точка входа — консольная демонстрация для всех типов + операции множества. */
  def main(args: Array[String]): Unit = {
    for typeName <- UserFactory.getTypeNameList() do
      println("\n============================================")
      println(s"  Тип: $typeName")
      println("============================================")
      UserFactory.getBuilderByName(typeName).foreach(MainDemo.testType)

    MainDemo.testSetOperations()
  }
}