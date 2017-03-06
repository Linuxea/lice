/**
 * Created by ice1000 on 2017/2/12.
 *
 * @author ice1000
 */
@file:JvmName("Model")
@file:JvmMultifileClass

package lice.compiler.model

import lice.compiler.model.MetaData.Factory.EmptyMetaData
import lice.compiler.model.Value.Objects.Nullptr
import lice.compiler.util.Func
import lice.compiler.util.ParseException.Factory.undefinedFunction
import lice.compiler.util.ParseException.Factory.undefinedVariable
import lice.compiler.util.SymbolList

class MetaData(
		val lineNumber: Int) {
	companion object Factory {
		val EmptyMetaData = MetaData(-1)
	}
}

class Value(
		val o: Any?,
		val type: Class<*>) {
	constructor(
			o: Any
	) : this(o, o.javaClass)

	companion object Objects {
		val Nullptr =
				Value(null, Any::class.java)
	}
}

interface Node {
	fun eval(): Value
	val meta: MetaData

	companion object Objects {
		fun getNullNode(meta: MetaData) =
				EmptyNode(meta)
	}
}

class ValueNode
@JvmOverloads
constructor(
		val value: Value,
		override val meta: MetaData = EmptyMetaData) : Node {

	@JvmOverloads
	constructor(
			any: Any,
			meta: MetaData = EmptyMetaData
	) : this(
			Value(any),
			meta
	)

	@JvmOverloads
	constructor(
			any: Any?,
			type: Class<*>,
			meta: MetaData = EmptyMetaData
	) : this(
			Value(any, type),
			meta
	)

	override fun eval(): Value {
//		println("老子求值了：${value.o}")
		return value
	}
}

//class JvmReflectionNode(
//		val methodName: String,
//		val receiver: Node,
//		val params: List<Node>) : Node {
//	override fun eval() = Value(receiver.eval().type.getMethod(
//			methodName,
//			*params
//					.map { it.eval().type }
//					.toTypedArray()
//	).invoke(
//			receiver,
//			*params
//					.map { it.eval().o }
//					.toTypedArray()
//	))
//}

class ExpressionNode(
		val symbolList: SymbolList,
		val function: String,
		override val meta: MetaData,
		val params: List<Node>) : Node {

	constructor(
			symbolList: SymbolList,
			function: String,
			meta: MetaData,
			vararg params: Node
	) : this(
			symbolList,
			function,
			meta,
			params.toList()
	)

	override fun eval() =
			(symbolList.getFunction(function)
					?: undefinedFunction(function, meta))
					.invoke(meta, params).eval()
}

class LambdaNode(
		val lambda: Func,
		val params: List<Node>,
		override val meta: MetaData) : Node {
	override fun eval() = lambda.invoke(meta, params).eval()
}

class SymbolNode(
		val symbolList: SymbolList,
		val name: String,
		override val meta: MetaData) : Node {

	override fun eval() =
			(symbolList.getVariable(name)
					?: undefinedVariable(name, meta))
					.eval()
}

class EmptyNode(override val meta: MetaData) : Node {
	override fun eval() = Nullptr
}

class Ast(
		val root: Node
)
