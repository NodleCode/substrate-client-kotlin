package io.nodle.substratesdk.types

/**
 * @author Lucien Loiseau on 30/07/20.
 */

const val META_MAGIC_NUMBER = 1635018093

data class RuntimeMetadata(
    var version: Int = 0,
    var magicNumber: Int = META_MAGIC_NUMBER,
    var modules: List<Module> = ArrayList(),
    var extrinsic: Extrinsic = Extrinsic()
) {
    data class Module(
        var name: String = "",
        var storage: Storage? = Storage(),
        var calls: List<Call>? = ArrayList(),
        var events: List<Event>? = ArrayList(),
        var constants: List<Constant> = ArrayList(),
        var errors: List<Error> = ArrayList(),
        var index: Int = 0
    )

    data class Storage(
        var prefix: String = "",
        var entries: List<Entry> = ArrayList()
    ) {
        data class Entry(
            var name: String = "",
            var modifier: Modifier = Modifier.OPTIONAL,
            var type: Pair<Int,Any>,
            var default: ByteArray = ByteArray(0),
            var documentation: List<String> = ArrayList()
        ) {
            enum class Modifier {
                OPTIONAL, DEFAULT, REQUIRED
            }

            enum class Hasher {
                BLAKE2_128, BLAKE2_256, BLAKE2_256_CONCAT, TWOX_128, TWOX_256, TWOX_64_CONCAT, IDENTITY
            }
        }

        data class TypePlain(
            var value: String
        )

        data class TypeMap(
            var hasher: Entry.Hasher = Entry.Hasher.BLAKE2_128,
            var key: String = "",
            var type: String = "",
            var iterable: Boolean = false
        )

        data class TypeDoubleMap(
            var firstHasher: Entry.Hasher = Entry.Hasher.BLAKE2_128,
            var firstKey: String = "",
            var secondHasher: Entry.Hasher = Entry.Hasher.BLAKE2_128,
            var secondKey: String = "",
            var type: String = ""
        )
    }

    data class Call(
        var moduleIndex: Int = 0,
        var callIndex: Int = 0,
        var name: String = "",
        var arguments: List<Argument> = ArrayList(),
        var documentation: List<String> = ArrayList()
    ) {
        data class Argument(
            var name: String = "",
            var type: String = ""
        )
    }

    data class Event(
        var name: String = "",
        var arguments: List<Argument> = ArrayList(),
        var documentation: List<String> = ArrayList()
    ) {
        data class Argument(
            var name: String = ""
        )
    }

    data class Constant(
        var name: String = "",
        var type: String = "",
        var values: ByteArray = ByteArray(0),
        var documentation: List<String> = ArrayList()
    )

    data class Error(
        var name: String = "",
        var documentation: List<String> = ArrayList()
    )

    data class Extrinsic(
        var version: Int = 4,
        var signedExtensions: List<String> = ArrayList()
    )
}

fun RuntimeMetadata.findCall(module: String, call: String) : RuntimeMetadata.Call? {
    return this.modules
        .find { it.name == module }?.calls
        ?.find { it.name == call }
}