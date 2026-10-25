package lab2.gui

import lab2.list.SinglyLinkedList
import lab2.persist.{JsonSerializer, Serializer, TextSerializer}
import lab2.types.{Comparator, UserFactory, UserType}

import scalafx.application.JFXApp3
import scalafx.Includes.*
import scalafx.collections.ObservableBuffer
import scalafx.geometry.{Insets, Pos}
import scalafx.scene.{Node, Scene}
import scalafx.scene.control.*
import scalafx.scene.layout.{HBox, Priority, VBox}
import scalafx.stage.FileChooser

import java.nio.file.Path

/**
 * GUI demonstration on ScalaFX (Variant A).
 * Programmatic layout without FXML.
 *
 * Window structure (analog of hello-view.fxml from lab1):
 *   Row 1: ComboBox (type) + TextField (value) + "Add to end" + "Find"
 *   Row 2: TextField (index) + "Insert" + "Get" + "Remove"
 *   Row 3: "Sort (in-place)" + "Sort (new)" + ToggleButton (format)
 *            + "Save" + "Load" + "Clear"
 *   ListView - content display
 *   Label - status bar
 */
object GuiDemo extends JFXApp3 {

  // ==================== State ====================

  private var currentList = new SinglyLinkedList[AnyRef]()
  private var currentPrototype: Option[UserType[?]] = None
  private var skipClearOnTypeChange = false
  private var serializer: Serializer[AnyRef] = new TextSerializer[AnyRef]()
  private var fileExtension = ".txt"

  // ==================== Controls ====================

  // lazy val — must NOT be initialized before JFX toolkit starts
  private lazy val typeSelector = new ComboBox[String]()
  private lazy val valueField = new TextField()
  private lazy val indexField = new TextField()
  private lazy val formatButton = new ToggleButton("Format: TXT")
  private lazy val listView = new ListView[String]()
  private lazy val statusLabel = new Label("Select data type to start")

  // ==================== Launch ====================

  override def start(): Unit = {
    valueField.promptText = "{1, 5, 10} or text"
    indexField.promptText = "0"
    indexField.prefWidth = 70

    val typeNames = UserFactory.getTypeNameList()
    typeSelector.items = ObservableBuffer(typeNames*)
    if (typeNames.nonEmpty) {
      typeSelector.value = typeNames.head
      onTypeChanged()
    }

    typeSelector.onAction = _ => onTypeChanged()
    formatButton.onAction = _ => onToggleFormat()

    stage = new JFXApp3.PrimaryStage {
      title = "lab2_kitpo - Singly Linked List"
      scene = new Scene(750, 550) {
        root = buildLayout()
      }
    }
  }
  // ==================== Layout ====================

  private def buildLayout(): VBox = {
    val row1 = new HBox(10)
    row1.alignment = Pos.CenterLeft
    row1.children = ObservableBuffer[Node](
      new Label("Type:"),
      typeSelector,
      new Label("Value:"),
      valueField,
      makeButton("Add End", onAddEnd, default = true),
      makeButton("Find", onFind)
    )
    HBox.setHgrow(valueField, Priority.Always)

    val row2 = new HBox(10)
    row2.alignment = Pos.CenterLeft
    row2.children = ObservableBuffer[Node](
      new Label("Index:"),
      indexField,
      makeButton("Insert", onInsert),
      makeButton("Get", onGet),
      makeButton("Remove", onRemove)
    )

    val row3 = new HBox(10)
    row3.alignment = Pos.CenterLeft
    row3.children = ObservableBuffer[Node](
      makeButton("Sort (in-place)", onSortImperative),
      makeButton("Sort (new)", onSortFunctional),
      formatButton,
      makeButton("Save", onSave),
      makeButton("Load", onLoad),
      makeButton("Clear", onClear)
    )

    val rootPane = new VBox(10)
    rootPane.padding = Insets(15)
    rootPane.children = ObservableBuffer[Node](row1, row2, row3, listView, statusLabel)
    VBox.setVgrow(listView, Priority.Always)
    rootPane
  }

  private def makeButton(text: String, action: () => Unit, default: Boolean = false): Button = {
    val btn = new Button(text)
    btn.onAction = _ => action()
    if (default) btn.defaultButton = true
    btn
  }

  // ==================== Event handlers ====================

  private def onTypeChanged(): Unit = {
    val name = typeSelector.value.value
    if (name == null) return
    currentPrototype = UserFactory.getBuilderByName(name)
    if (currentPrototype.isEmpty) return
    if (!skipClearOnTypeChange) {
      currentList = new SinglyLinkedList[AnyRef]()
    }
    updateValuePrompt()
    updateListView()
    statusLabel.text = s"Type: $name.  Elements: ${currentList.size}"
  }

  private def onAddEnd(): Unit = {
    if (!checkPrototype()) return
    try {
      val proto = currentPrototype.get
      val item = proto.parseValue(valueField.text.value)
      currentList.addToEnd(item)
      updateListView()
      statusLabel.text = s"Added to end: $item"
      valueField.clear()
    } catch {
      case e: Exception => statusLabel.text = s"Input error: ${e.getMessage}"
    }
  }

  private def onInsert(): Unit = {
    if (!checkPrototype()) return
    try {
      val index = parseIndex()
      val proto = currentPrototype.get
      val item = proto.parseValue(valueField.text.value)
      currentList.insert(index, item)
      updateListView()
      statusLabel.text = s"Inserted [$index]: $item"
      valueField.clear()
    } catch {
      case _: NumberFormatException => statusLabel.text = "Error: index must be integer"
      case e: Exception => statusLabel.text = s"Error: ${e.getMessage}"
    }
  }

  private def onGet(): Unit = {
    if (!checkPrototype()) return
    try {
      val index = parseIndex()
      val item = currentList.get(index)
      statusLabel.text = s"Element [$index] = $item"
      listView.selectionModel.value.select(index)
    } catch {
      case _: NumberFormatException => statusLabel.text = "Error: index must be integer"
      case e: Exception => statusLabel.text = s"Error: ${e.getMessage}"
    }
  }

  private def onRemove(): Unit = {
    if (!checkPrototype()) return
    try {
      val index = parseIndex()
      val removed = currentList.remove(index)
      updateListView()
      statusLabel.text = s"Removed [$index]: $removed"
    } catch {
      case _: NumberFormatException => statusLabel.text = "Error: index must be integer"
      case e: Exception => statusLabel.text = s"Error: ${e.getMessage}"
    }
  }

  private def onSortImperative(): Unit = {
    if (!checkPrototype()) return
    try {
      val comp = currentPrototype.get.getTypeComparator().asInstanceOf[Comparator[AnyRef]]
      currentList.sortImperative(comp)
      updateListView()
      statusLabel.text = "List sorted (imperative, in-place)"
    } catch {
      case e: Exception => statusLabel.text = s"Sort error: ${e.getMessage}"
    }
  }

  private def onSortFunctional(): Unit = {
    if (!checkPrototype()) return
    try {
      val comp = currentPrototype.get.getTypeComparator().asInstanceOf[Comparator[AnyRef]]
      val sorted = currentList.sortFunctional(comp)
      currentList = sorted
      updateListView()
      statusLabel.text = "List sorted (functional, new list)"
    } catch {
      case e: Exception => statusLabel.text = s"Sort error: ${e.getMessage}"
    }
  }

  private def onFind(): Unit = {
    if (!checkPrototype()) return
    try {
      val search = valueField.text.value
      currentList.firstThat(obj => obj.toString.contains(search)) match {
        case Some(found) =>
          statusLabel.text = s"Found: $found"
          val idx = currentList.toList.indexOf(found)
          if (idx >= 0) listView.selectionModel.value.select(idx)
        case None =>
          statusLabel.text = s"Not found: \"$search\""
      }
    } catch {
      case e: Exception => statusLabel.text = s"Search error: ${e.getMessage}"
    }
  }

  private def onToggleFormat(): Unit = {
    if (formatButton.selected.value) {
      serializer = new JsonSerializer[AnyRef]()
      fileExtension = ".json"
      formatButton.text = "Format: JSON"
      statusLabel.text = "Serialization format: JSON"
    } else {
      serializer = new TextSerializer[AnyRef]()
      fileExtension = ".txt"
      formatButton.text = "Format: TXT"
      statusLabel.text = "Serialization format: TXT"
    }
  }

  private def onSave(): Unit = {
    if (!checkPrototype()) return
    try {
      val fc = new FileChooser()
      fc.title = "Save list"
      if (fileExtension == ".json") {
        fc.extensionFilters += new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
      } else {
        fc.extensionFilters += new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
      }
      val file = fc.showSaveDialog(stage)
      if (file == null) return
      serializer.save(currentPrototype.get, currentList, file.toPath)
      statusLabel.text = s"Saved: ${file.getName}"
    } catch {
      case e: Exception => statusLabel.text = s"Save error: ${e.getMessage}"
    }
  }

  private def onLoad(): Unit = {
    if (!checkPrototype()) return
    try {
      val fc = new FileChooser()
      fc.title = "Load list"
      if (fileExtension == ".json") {
        fc.extensionFilters += new FileChooser.ExtensionFilter("JSON files (*.json)", "*.json")
      } else {
        fc.extensionFilters += new FileChooser.ExtensionFilter("Text files (*.txt)", "*.txt")
      }
      val file = fc.showOpenDialog(stage)
      if (file == null) return
      val path = file.toPath

      val loader: Serializer[AnyRef] =
        if (path.toString.toLowerCase.endsWith(".json")) new JsonSerializer[AnyRef]()
        else new TextSerializer[AnyRef]()

      loader.peekTypeName(path) match {
        case Some(fileTypeName) if Some(fileTypeName) != currentPrototype.map(_.typeName()) =>
          skipClearOnTypeChange = true
          typeSelector.value = fileTypeName
          skipClearOnTypeChange = false
        case _ =>
      }

      currentList = loader.load(path)

      if (loader.isInstanceOf[JsonSerializer[?]]) {
        serializer = new JsonSerializer[AnyRef]()
        fileExtension = ".json"
        formatButton.selected = true
        formatButton.text = "Format: JSON"
      } else {
        serializer = new TextSerializer[AnyRef]()
        fileExtension = ".txt"
        formatButton.selected = false
        formatButton.text = "Format: TXT"
      }

      updateListView()
      statusLabel.text = s"Loaded ${currentList.size} elements from ${file.getName}"
    } catch {
      case e: Exception => statusLabel.text = s"Load error: ${e.getMessage}"
    }
  }

  private def onClear(): Unit = {
    currentList = new SinglyLinkedList[AnyRef]()
    updateListView()
    statusLabel.text = "List cleared"
  }

  // ==================== Utilities ====================

  private def checkPrototype(): Boolean = {
    if (currentPrototype.isEmpty) {
      statusLabel.text = "Please select data type first"
      false
    } else true
  }

  private def parseIndex(): Int = indexField.text.value.trim.toInt

  private def updateListView(): Unit = {
    listView.items = ObservableBuffer(currentList.toList.map(_.toString)*)
  }

  private def updateValuePrompt(): Unit = {
    currentPrototype.foreach { proto =>
      valueField.promptText = proto.typeName() match {
        case "IntegerBitSet" => "{1, 5, 10} or 1 5 10"
        case "String"        => "Enter text"
        case _               => "Value"
      }
    }
  }
}