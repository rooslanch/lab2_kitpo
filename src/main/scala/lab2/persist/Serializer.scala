package lab2.persist

import lab2.list.SinglyLinkedList
import lab2.types.UserType
import java.nio.file.Path

/**
 * Абстракция сериализатора структуры данных.
 * Позволяет подменять формат хранения (текст, JSON и т.д.)
 * без изменений в GUI и демонстрационном main.
 *
 * @tparam A тип элементов списка
 */
trait Serializer[A]:

  /**
   * Сохраняет список в файл.
   *
   * @param typePrototype прототип типа — для определения имени типа
   * @param list          сохраняемый список
   * @param path          путь к файлу
   */
  def save(typePrototype: UserType[?], list: SinglyLinkedList[A], path: Path): Unit

  /**
   * Читает только имя типа из файла, не загружая данные.
   *
   * @param path путь к файлу
   * @return имя типа или [[None]], если не удалось определить
   */
  def peekTypeName(path: Path): Option[String]

  /**
   * Загружает список из файла.
   * Имя типа читается из файла, затем по нему из [[lab2.types.UserFactory]]
   * берётся прототип для парсинга элементов.
   *
   * @param path путь к файлу
   * @return новый список с загруженными элементами
   */
  def load(path: Path): SinglyLinkedList[A]