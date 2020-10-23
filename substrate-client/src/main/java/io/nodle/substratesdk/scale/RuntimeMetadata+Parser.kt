package io.nodle.substratesdk.scale

import io.nodle.substratesdk.types.RuntimeMetadata
import io.nodle.substratesdk.types.RuntimeMetadata.*
import io.nodle.substratesdk.types.RuntimeMetadata.Storage.Entry
import io.nodle.substratesdk.utils.onDebugOnly
import java.nio.ByteBuffer
import java.nio.ByteOrder

/**
 * @author Lucien Loiseau on 30/07/20.
 */
@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readMetadata(): RuntimeMetadata {
    order(ByteOrder.LITTLE_ENDIAN)
    val metadata = RuntimeMetadata()
    metadata.magicNumber = readI32()
    metadata.version = readU8().toInt()
    when (metadata.version) {
        11 -> metadata.modules = readListWithCounter { i -> readMetadataModuleV11(i) }
        12 -> metadata.modules = readListWithCounter { i -> readMetadataModuleV12(i) }
        else -> throw ScaleParserException("unsupported metadata version: ${metadata.version}")
    }
    return metadata
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readMetadataModuleV11(i: Int): Module {
    return Module(
        name = readString(),
        storage = readOptional { readModuleStorage() },
        calls = readOptional { readListWithCounter { j -> readModuleCall(i, j) } },
        events = readOptional { readList { readModuleEvent() } },
        constants = readList { readModuleConstant() },
        errors = readList { readModuleError() },
        index = i
    )
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readMetadataModuleV12(i: Int): Module {
    val moduleV12 = Module(
        name = readString(),
        storage = readOptional { readModuleStorage() },
        calls = readOptional { readListWithCounter { j -> readModuleCall(i, j) } },
        events = readOptional { readList { readModuleEvent() } },
        constants = readList { readModuleConstant() },
        errors = readList { readModuleError() },
        index = readU8().toInt()
    )
    // in MetadataV12, module index is no longer the index of the variant in the module enum
    // but is instead set as a field. Thus, for each call of the module,
    // we need to update the module index with the correct one
    // for more details: https://github.com/paritytech/substrate/pull/6969
    moduleV12.calls?.forEach {  it.moduleIndex = moduleV12.index }
    return moduleV12
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleStorage(): Storage {
    return Storage(
        prefix = readString(),
        entries = readList { readModuleStorageEntry() })
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleStorageEntry(): Entry {
    return Entry(
        name = readString(),
        modifier = readEnum(Entry.Modifier.values()),
        type = readUnion(
            { readModuleStorageTypePlain() },
            { readModuleStorageTypeMap() },
            { readModuleStorageTypeDoubleMap() }
        ),
        default = readByteArray(),
        documentation = readList { readString() })
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleStorageTypePlain(): Storage.TypePlain {
    return Storage.TypePlain(
        value = readString()
    )
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleStorageTypeMap(): Storage.TypeMap {
    return Storage.TypeMap(
        hasher = readEnum(Entry.Hasher.values()),
        key = readString(),
        type = readString(),
        iterable = readBoolean()
    )
}

@ExperimentalUnsignedTypes
@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleStorageTypeDoubleMap(): Storage.TypeDoubleMap {
    return Storage.TypeDoubleMap(
        firstHasher = readEnum(Entry.Hasher.values()),
        firstKey = readString(),
        secondKey = readString(),
        type = readString(),
        secondHasher = readEnum(Entry.Hasher.values())
    )
}


@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleCall(moduleIndex: Int, callIndex: Int): Call {
    return Call(
        moduleIndex = moduleIndex,
        callIndex = callIndex,
        name = readString(),
        arguments = readList { readCallArgument() },
        documentation = readList { readString() })
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readCallArgument(): Call.Argument {
    return Call.Argument(
        name = readString(),
        type = readString()
    )
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleEvent(): Event {
    return Event(
        name = readString(),
        arguments = readList { readEventArgument() },
        documentation = readList { readString() })
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readEventArgument(): Event.Argument {
    return Event.Argument(
        name = readString()
    )
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleConstant(): Constant {
    return Constant(
        name = readString(),
        type = readString(),
        values = readByteArray(),
        documentation = readList { readString() })
}

@Throws(ScaleParserException::class)
fun ByteBuffer.readModuleError(): Error {
    return Error(
        name = readString(),
        documentation = readList { readString() }
    )
}
