# TODO: Портирование lab1 (Java) → lab2 (Scala 3)

## Референс для порта

Java-исходники и полная архитектурная справка:

```
C:\Users\Руслан\IdeaProjects\lab1_kitpo\lab1_kitpo
```

Ключевые файлы:
- `README.md` — описание трёхслойной архитектуры, сортировки merge sort, тонкости сериализации
- `src/main/java/com/example/lab1_kitpo/MainDemo.java` — консольная демонстрация
- `src/main/java/com/example/lab1_kitpo/list/SinglyLinkedList.java` — СД (односвязный список)
- `src/main/java/com/example/lab1_kitpo/type/UserType.java` — интерфейс типовых данных
- `src/main/java/com/example/lab1_kitpo/type/impl/IntegerBitSet.java` — множество целых (вариант 10)
- `src/main/java/com/example/lab1_kitpo/type/impl/StringType.java` — строка (второй тип)
- `src/main/java/com/example/lab1_kitpo/type/UserFactory.java` — фабрика прототипов
- `src/main/java/com/example/lab1_kitpo/persist/Serializer.java`, `TextSerializer.java`, `JsonSerializer.java` — персистентность
- `src/main/java/com/example/lab1_kitpo/HelloController.java` — GUI (JavaFX)

---

## Фаза 1. Типовые данные (Layer 1)

- [x] 1.1 `trait UserType[T <: AnyRef]` (`typeName`, `create`, `copy`, `parseValue`, `readValue`, `getTypeComparator`) — generic; `clone()` переименовано в `copy()` (конфликт с protected `Object.clone()`)
- [x] 1.2 `type Comparator[A] = (A, A) => Int` — type alias, не trait; передаётся явно в сортировку
- [x] 1.3 `object UserFactory` (singleton, `LinkedHashMap[String, UserType[?]]`, `getTypeNameList`, `getBuilderByName: Option`)
- [x] 1.4 `class IntegerBitSet` (`BitSet`, `add`, `contains`, `union`, `intersection`, `difference`, `cardinality`, `parseValue`, `toString`)
- [x] 1.5 `class StringType` (обёртка над `String`, `parseValue` тривиальный)

## Фаза 2. Структура данных (Layer 2) — `SinglyLinkedList[A]`

- [x] 2.1 Узел: `case class Node[A](var data: A, var next: Option[Node[A]])` — в companion object
- [x] 2.2 Базовые операции: `addToEnd(value)`, `get(index)`, `insert(index, value)`, `remove(index)` — императивные, `tail` для O(1) добавления
- [x] 2.3 `forEach(f: A => Unit): Unit` — параметр = **функция** (требование lab2)
- [x] 2.4 `firstThat(f: A => Boolean): Option[A]` — возвращает `Option` вместо nullable
- [x] 2.5 **Императивная сортировка:** `sortImperative(comp: Comparator[A]): Unit` — merge sort через **перестановку** `next` (in-place, мутирует текущий список)
- [x] 2.6 **Функциональная сортировка:** `sortFunctional(comp: Comparator[A]): SinglyLinkedList[A]` — immutable merge sort через `List[A]`, возвращает новый список
- [x] 2.7 Вспомогательное: `toList: List[A]`, `toString`, `size`, `isEmpty`

## Фаза 3. Сериализация (persist layer)

- [x] 3.1 `trait Serializer[A]` (`save`, `load`, `peekTypeName`) — `Option[String]` вместо nullable
- [x] 3.2 `TextSerializer[A]` — формат как в lab1: первая строка `typeName`, далее `toString()` каждого элемента
- [x] 3.3 `JsonSerializer[A]` — JSON `org.json` с полем `typeName` и массивом строк `elements`

## Фаза 4. Консольная демонстрация (`MainDemo`)

- [x] 4.1 `testType(proto: UserType)` — единый сценарий для всех типов: `addToEnd` → `insert` → `get` → `remove` → `forEach` → обе сортировки → `firstThat` → `save` → `load` → `toString`
- [x] 4.2 `testSetOperations()` — `union`, `intersection`, `difference`, `add`, `contains` на `IntegerBitSet`
- [x] 4.3 Entry point: `@main def main(): Unit` — вызов `testType` для всех типов + `testSetOperations`
- [x] 4.4 Демонстрация **обеих сортировок** на одном и том же наборе данных (сначала imperative, затем functional)

## Фаза 5. GUI (Layer 3) — Вариант A: ScalaFX

- [x] 5.1 Добавить зависимость ScalaFX в `build.sbt`
- [x] 5.2 FXML-разметка: адаптировать `hello-view.fxml` из lab1 или создать новый ScalaFX-контроллер без FXML
- [x] 5.3 ScalaFX-контроллер (аналог `HelloController.java`): выбор типа, addToEnd, insert, remove, sort, forEach, save/load
- [x] 5.4 Интеграция с `SinglyLinkedList`, `UserFactory`, `Serializer`
- [x] 5.5 Entry point GUI: `@main def main(): Unit = JFXApp3` или отдельный объект-наследник `scalafx.application.JFXApp3`

## Фаза 6. Интеграция и финализация

- [ ] 6.1 `sbt compile` — 0 ошибок компиляции
- [x] 6.2 `sbt run` (MainDemo) — все операции проходят, **обе сортировки** дают корректный результат
- [ ] 6.3 GUI-тест: кнопки, save/load, переключение типов, отображение списка
- [ ] 6.4 Обновить `README.md` в `lab2_kitpo` (архитектура, запуск, сортировки)
- [ ] 6.5 Диаграмма классов для отчёта

---

## Утверждённые архитектурные решения

1. **Generics:** `SinglyLinkedList[A]` с реальными generics (не `Object`).
2. **Null-safety:** `Option[Node[A]]` вместо `null` для ссылок, `Option[A]` для `firstThat`.
3. **Компаратор:** `type Comparator[A] = (A, A) => Int` — функция, передаётся явно в `sort` (не хранится в `UserType`).
4. **Интерфейсы обратного вызова:** `DoWith` / `TestIt` удаляются; `forEach` и `firstThat` принимают функции напрямую.
5. **Две сортировки:** `sortImperative` (in-place, mutable) + `sortFunctional` (immutable, новый список).
6. **Entry point:** Scala 3 синтаксис `@main def main(): Unit`.
7. **GUI:** **ScalaFX** (вариант A).
