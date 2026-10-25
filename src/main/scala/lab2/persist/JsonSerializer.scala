package lab2.persist

import lab2.list.SinglyLinkedList
import lab2.types.{UserFactory, UserType}
import org.json.{JSONArray, JSONObject}
import java.io.IOException
import java.nio.charset.StandardCharsets
import java.nio.file.{Files, Path}

/**
 * JSON-сериализация/десериализация структуры данных.
 *
 * Формат файла (pretty-printed JSON):
 * {{{
 * {
 *   "typeName": "IntegerBitSet",
 *   "elements": [
 *     "{1, 5, 10}",
 *     "{3, 7}",
 *     "{2}"
 *   ]
 * }
 * }}}
 *
 * Поле `typeName` определяет, какой прототип из [[UserFactory]]
 * использовать для парсинга строк элементов. Сами элементы хранятся как
 * результат `toString()`, что позволяет не привязывать формат к
 * внутреннему устройству конкретного типа.
 */
class JsonSerializer[A] extends Serializer[A]:

  private val KeyType     = "typeName"
  private val KeyElements = "elements"

  override def save(typePrototype: UserType[?], list: SinglyLinkedList[A], path: Path): Unit =
    val root = JSONObject()
    root.put(KeyType, typePrototype.typeName())

    val arr = JSONArray()
    list.forEach(obj => arr.put(obj.toString))
    root.put(KeyElements, arr)

    Files.writeString(path, root.toString(2), StandardCharsets.UTF_8)

  override def peekTypeName(path: Path): Option[String] =
    val content = Files.readString(path, StandardCharsets.UTF_8)
    val root = JSONObject(content)
    Option(root.optString(KeyType, null)).filter(_.nonEmpty)

  override def load(path: Path): SinglyLinkedList[A] =
    val content = Files.readString(path, StandardCharsets.UTF_8)
    val root = JSONObject(content)

    val typeName = root.getString(KeyType)
    val proto = UserFactory.getBuilderByName(typeName).getOrElse(
      throw new IOException(s"""Неизвестный тип: "$typeName"""")
    )

    val arr = root.getJSONArray(KeyElements)
    val list = SinglyLinkedList[A]()
    for i <- 0 until arr.length() do
      list.addToEnd(proto.parseValue(arr.getString(i)).asInstanceOf[A])
    list