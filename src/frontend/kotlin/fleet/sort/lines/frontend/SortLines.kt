package fleet.sort.lines.frontend

import andel.editor.*
import andel.operation.Operation
import andel.operation.Sticky
import andel.operation.compose
import andel.operation.transformOnto
import andel.text.TextLine
import com.jetbrains.rhizomedb.Entrypoint
import fleet.frontend.actions.ActionsEP
import fleet.frontend.actions.writableDocumentAction
import fleet.frontend.actions.writeEditorAction
import fleet.kernel.ChangeScope
import fleet.kernel.register
import noria.model.Action
import noria.model.Trigger

// remove it after opening internals
internal fun MutableEditor.editForEachCaret(f: (Caret) -> Pair<Operation, Caret?>) {
  val emptyOp = Operation.empty(document.text.charsCount())

  val opsAndCarets = carets.fold(emptyOp to emptyList<Caret>()) { (comp, updatedCarets), caret ->
    val (op, caretPrime) = f(caret.copy(position = caret.position.transformOnto(comp, Sticky.RIGHT)))
    document.edit(op)
    val compPrime = comp.compose(op)
    val caretsPrime = ArrayList<Caret>()
    updatedCarets.mapTo(caretsPrime) { c ->
      c.copy(position = c.position.transformOnto(op, Sticky.RIGHT))
    }
    if (caretPrime != null) {
      caretsPrime.add(caretPrime)
    }
    compPrime to caretsPrime
  }

  moveCarets(opsAndCarets.second)
}

fun MutableEditor.sortLines() {
  command(EditorCommandType.EDIT) {
    val text = document.text

    editForEachCaret { caret: Caret ->
      when {
        caret.hasSelection() -> {
          val startLine = text.lines().atCharOffset(caret.position.selectionStart)
          val endLine = text.lines().atCharOffset(caret.position.selectionEnd)
          val start = startLine.fromChar
          val end = endLine.toCharExcludingSeparator

          val before = text.charSequence(start, end).toString()

          val sorted = mutableListOf<String>()
          var cur: TextLine? = startLine
          while (cur != null && cur.lineNumber <= endLine.lineNumber) {
            sorted.add(cur.asCharSequence().toString())
            cur = cur.next()
          }

          sorted.sort()
          val newText = sorted.joinToString(separator = "\n")

          val op = Operation.replaceAt(start, before, newText, text.charsCount())
          op to caret
        }

        else -> {
          Operation.empty() to caret
        }
      }
    }
  }
}


@Entrypoint
fun ChangeScope.entrypoint() {
  register {
    ActionsEP.register {
      writableDocumentAction(
              identifier = Action.Identifier(sortLines.ident),
              name = "Sort Lines",
              perform = writeEditorAction { sortLines() },
              trigger = sortLines)
    }
  }
}

private val sortLines = Trigger("sort-lines")
