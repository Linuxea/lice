/**
 * Created by ice1000 on 2017/2/12.
 *
 * @author ice1000
 */
package lice.compiler.parse

import lice.compiler.model.*
import lice.compiler.util.SymbolList
import lice.compiler.util.debugApply
import lice.compiler.util.debugOutput
import java.io.File
import java.util.*

fun buildNode(code: String): StringNode {
	var beginIndex = 0
	val currentNodeStack = Stack<StringMiddleNode>()
	currentNodeStack.push(StringMiddleNode())
	var elementStarted = true
	var lineNumber = 0
	fun check(index: Int) {
		if (elementStarted) {
			elementStarted = false
			currentNodeStack
					.peek()
					.list
					.add(StringLeafNode(code
							.substring(startIndex = beginIndex, endIndex = index)
							.debugApply { println("found token: $this") }
					))
		}
	}
	code.forEachIndexed { index, c ->
		when (c) {
			'(' -> {
				check(index)
				currentNodeStack.push(StringMiddleNode())
				++beginIndex
			}
			')' -> {
				check(index)
				if (currentNodeStack.size <= 1) {
					println("Braces not match at line $lineNumber: Unexpected \')\'.")
					return EmptyStringNode
				}
				val son =
						if (currentNodeStack.peek().empty) EmptyStringNode
						else currentNodeStack.peek()
				currentNodeStack.pop()
				currentNodeStack.peek().add(son)
			}
			' ', '\n', '\t' -> {
				check(index)
				beginIndex = index + 1
				if (c == '\n') ++lineNumber
			}
			else -> elementStarted = true
		}
	}
	check(code.length - 1)
	if (currentNodeStack.size > 1) {
		println("Braces not match at line $lineNumber: Expected \')\'.")
	}
	return currentNodeStack.peek()
}

fun createAst(file: File): Ast {
	val code = file.readText()
	val symbolList = SymbolList()
	symbolList.initialize()
	symbolList.addVariable("FILE_PATH", Value(file.absolutePath))
	val stringTreeRoot = buildNode(code)
	fun test(node: StringNode): Node {
		when (node) {
			is StringMiddleNode -> {
				node.list
			}
			is StringLeafNode -> {
				node.str.debugOutput()
			}
		}
		TODO()
	}
	return Ast(test(stringTreeRoot), symbolList)
}