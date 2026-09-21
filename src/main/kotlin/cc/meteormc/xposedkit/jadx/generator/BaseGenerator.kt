package cc.meteormc.xposedkit.jadx.generator

import cc.meteormc.xposedkit.jadx.data.MatchMode
import jadx.core.deobf.conditions.JadxRenameConditions
import jadx.core.dex.instructions.args.ArgType
import jadx.core.dex.nodes.ClassNode
import jadx.core.dex.nodes.FieldNode
import jadx.core.dex.nodes.ICodeNode
import jadx.core.dex.nodes.MethodNode
import java.util.EnumSet
import kotlin.text.toHexString

abstract class BaseGenerator<T : ICodeNode> {
    private val renameCondition = JadxRenameConditions.buildDefault()

    abstract fun generate(
        node: T,
        mode: MatchMode = getPreferredMode(node),
        useLambda: Boolean = true
    ): String

    abstract fun getPreferredMode(node: T): MatchMode

    abstract fun getDisabledModes(node: T): EnumSet<MatchMode>

    protected fun String.isBootClass(): Boolean {
        return startsWith("java.") || startsWith("android.")
    }

    protected fun String.capitalize(): String {
        return replaceFirstChar { s -> s.titlecase() }
    }

    protected fun String.quote(): String {
        val str = this
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
            .replace("\b", "\\b")
        // 直接使用插值前缀来转义$
        return "${if (contains('$')) "$$" else ""}\"$str\""
    }

    protected fun <T : ICodeNode> Iterable<T>.noneNode(self: T, predicate: (T) -> Boolean): Boolean {
        return none { it != self && predicate(it) }
    }

    protected fun Int.toModifiers(): String {
        return (this and 0xffff).toHexString(HexFormat {
            upperCase = true
            number.prefix = "0x"
        })
    }

    protected fun ClassNode.rawName(): String {
        // 获取原始类名
        return classInfo.makeRawFullName()
    }

    protected infix fun ArgType.typeEquals(other: ArgType): Boolean {
        return this.toReference() == other.toReference()
    }

    protected fun String.createSnippet(code: String, useLambda: Boolean): String {
        val ref = formatReference(false)
        return if (useLambda) {
            "$ref.reflect {\n${code.prependIndent("    ")}\n}"
        } else {
            "$ref.reflect${if (code.isNotBlank()) ".$code" else ""}"
        }
    }

    protected fun ArgType.toReference(): String {
        return when {
            isPrimitive -> {
                "${primitiveType.longName.capitalize()}::class.javaPrimitiveType!!"
            }
            isArray -> {
                var type = arrayElement
                if (type.isPrimitive) {
                    // 基本类型数组 直接用kt提供的数组类
                    "${type.primitiveType.longName.capitalize()}Array::class.java"
                } else {
                    var count = 1
                    while (type.isArray) {
                        count++
                        type = type.arrayElement
                    }

                    val typeName = when {
                        type.isPrimitive -> type.primitiveType.shortName
                        type.isGeneric -> type.`object`
                        type.isGenericType -> "java.lang.Object"
                        else -> type.toString()
                    }

                    // 对象数组或多维数组 需要反射获取
                    "${"${"[".repeat(count)}L$typeName;".quote()}.clazz"
                }
            }
            isGeneric -> {
                `object`.formatReference(true)
            }
            isGenericType -> {
                "Any::class.java"
            }
            else -> {
                toString().formatReference(true)
            }
        }
    }

    protected fun String.formatReference(toClass: Boolean): String {
        val suffix: String
        // 从BootClassLoader加载的类可以直接引用类型
        return if (isBootClass()) {
            suffix = ".java"
            if (this == "java.lang.Object") {
                "Any"
            } else {
                // 暂时选择去除包名
                split('.').last()
            } + "::class"
        } else {
            suffix = ".clazz!!"
            quote()
        } + if (toClass) {
            suffix
        } else {
            ""
        }
    }

    protected fun ICodeNode.isObfuscated(): Boolean {
        renameCondition.init(root())
        return when (this) {
            is ClassNode -> renameCondition.shouldRename(this)
            is MethodNode -> renameCondition.shouldRename(this)
            is FieldNode -> renameCondition.shouldRename(this)
            else -> false
        }
    }
}