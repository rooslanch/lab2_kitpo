package lab2.persist

import lab2.list.SinglyLinkedList
import lab2.types.{UserFactory, UserType}
import java.io.{IOException, PrintWriter}
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/**
 * Текстовая сериализация/десериализация структуры данных.
 * Формат файла:
 * {{{
 * строка 1:  имя типа (например, "IntegerBitSet")
 * строка 2+: toString() каждого элемента списка
 * }}}
 */
class TextSerializer[A] extends Serializer[A]:

  override def save(typePrototype: UserType[?], list: SinglyLinkedList[A], path: Path): Unit =
    val writer = new PrintWriter(Files.newBufferedWriter(path, StandardCharsets.UTF_8))
    try
      writer.println(typePrototype.typeName())
      list.forEach(obj => writer.println(obj.toString))
    finally
      writer.close()

  override def peekTypeName(path: Path): Option[String] =
    val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
    try
      Option(reader.readLine()).map(_.trim).filter(_.nonEmpty)
    finally
      reader.close()

  override def load(path: Path): SinglyLinkedList[A] =
    val reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)
    try
      val typeName = reader.readLine()
      if typeName == null || typeName.isBlank then
        throw new IOException(s"Файл пуст или не содержит имени типа: $path")
      val proto = UserFactory.getBuilderByName(typeName.trim).getOrElse(
        throw new IOException(s"""Неизвестный тип: "${typeName.trim}"""")
      )
      val list = SinglyLinkedList[A]()
      var line = reader.readLine()
      while line != null do
        list.addToEnd(proto.parseValue(line).asInstanceOf[A])
        line = reader.readLine()
      list
    finally
      reader.close()