package io.nodle.substratesdk.scale

import java.math.BigInteger
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.charset.Charset

class ScaleParserException(msg: String?) : Exception(msg)

inline fun parserRequire(value: Boolean, lazyMessage: () -> Any): Unit {
    if (!value) {
        val message = lazyMessage()
        throw ScaleParserException(message.toString())
    }
}

inline fun <T> catchAll(parser: () -> T): T {
    try {
        return parser()
    } catch (e: Exception) {
        throw ScaleParserException(e.message)
    }
}


@ExperimentalUnsignedTypes
fun ByteBuffer.readU8(): UByte = catchAll { order(ByteOrder.LITTLE_ENDIAN).get().toUByte() }


@ExperimentalUnsignedTypes
fun ByteBuffer.readU16(): UShort = catchAll { order(ByteOrder.LITTLE_ENDIAN).short.toUShort() }


@ExperimentalUnsignedTypes
fun ByteBuffer.readU32(): UInt = catchAll { order(ByteOrder.LITTLE_ENDIAN).int.toUInt() }

@ExperimentalUnsignedTypes
fun ByteBuffer.readU64(): ULong = catchAll { order(ByteOrder.LITTLE_ENDIAN).long.toULong() }

fun ByteBuffer.readU128(): BigInteger = catchAll {
    val ba = ByteArray(16)
    get(ba)
    BigInteger(ba.reversedArray())
}

fun ByteBuffer.readI8(): Byte = catchAll { order(ByteOrder.LITTLE_ENDIAN).get() }

fun ByteBuffer.readI16(): Short = catchAll { order(ByteOrder.LITTLE_ENDIAN).short }

fun ByteBuffer.readI32(): Int = catchAll { order(ByteOrder.LITTLE_ENDIAN).int }

fun ByteBuffer.readI64(): Long = catchAll { order(ByteOrder.LITTLE_ENDIAN).long }

fun ByteBuffer.readCompactInteger(): Int = catchAll {
    this.order(ByteOrder.LITTLE_ENDIAN)
    val byte = this.get().toInt() and 0xff
    if (byte and 0b11 == 0x00) {
        return (byte shr 2)
    }
    if (byte and 0b11 == 0x01) {
        this.position(position() - 1)
        return ((short.toInt() and 0xffff) shr 2)
    }
    if (byte and 0b11 == 0x02) {
        this.position(position() - 1)
        return (int shr 2)
    }
    throw ScaleParserException("compact mode not supported")
}

fun ByteBuffer.readCompactBigInt(): BigInteger = catchAll {
    this.order(ByteOrder.LITTLE_ENDIAN)
    val byte = this.get().toInt() and 0xff
    if (byte and 0b11 == 0x00) {
        return (byte shr 2).toBigInteger()
    }
    if (byte and 0b11 == 0x01) {
        this.position(position() - 1)
        return ((short.toInt() and 0xffff) shr 2).toBigInteger()
    }
    if (byte and 0b11 == 0x02) {
        this.position(position() - 1)
        return (int shr 2).toBigInteger()
    }

    val len = byte shr 2
    val ba = ByteArray(len)
    get(ba)
    return BigInteger(ba.reversedArray())
}

fun ByteBuffer.readString(): String = catchAll {
    this.order(ByteOrder.LITTLE_ENDIAN)
    val size = readCompactInteger()
    val ba = ByteArray(size)
    get(ba)
    return ba.toString(charset = Charset.defaultCharset())
}

fun <T> ByteBuffer.readList(elementParser: ByteBuffer.() -> T): List<T> = catchAll {
    val size = readCompactInteger()
    val list = ArrayList<T>(size)
    for (i in 0 until size) {
        list.add(this.elementParser())
    }
    return list
}

fun <T> ByteBuffer.readListWithCounter(elementParser: ByteBuffer.(i: Int) -> T): List<T> = catchAll {
    val size = readCompactInteger()
    val list = ArrayList<T>(size)
    for (i in 0 until size) {
        list.add(this.elementParser(i))
    }
    return list
}

fun ByteBuffer.readByteArray(): ByteArray = catchAll {
    this.order(ByteOrder.LITTLE_ENDIAN)
    val size = readCompactInteger()
    val ba = ByteArray(size)
    get(ba)
    return ba
}

fun ByteBuffer.readBoolean(): Boolean = catchAll { this.get() == 0x01.toByte() }

fun ByteBuffer.readOptionalBoolean(): Boolean? = catchAll {
    if (this.get() == 0x00.toByte()) {
        null
    } else {
        this.get() == 0x02.toByte()
    }
}

inline fun <reified T> ByteBuffer.readOptional(optionalParser: ByteBuffer.() -> T): T? = catchAll {
    return when (T::class) {
        Boolean::class -> readOptionalBoolean() as T?
        else -> {
            when (readBoolean()) {
                true -> this.optionalParser()
                else -> null
            }
        }
    }
}

@ExperimentalUnsignedTypes
fun <T : Enum<T>> ByteBuffer.readEnum(values: Array<T>): T = catchAll {
    val id = this.readU8().toInt()
    for (t in values) {
        if (t.ordinal == id) {
            return t
        }
    }
    throw ScaleParserException("Unknown enum value: $id")
}

@ExperimentalUnsignedTypes
fun ByteBuffer.readUnion(vararg parsers: ByteBuffer.() -> Any): Pair<Int, Any> = catchAll {
    val index = this.readU8().toInt()
    parserRequire(index <= 255) { "Union can have max 255 values. Index: $index" }
    parserRequire(index < parsers.size) { "Index unknown for this union: $index" }
    val parse = parsers[index]
    return Pair(index, this.parse())
}