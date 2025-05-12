package com.example.hobbyreads.util

import com.google.gson.TypeAdapter
import com.google.gson.stream.JsonReader
import com.google.gson.stream.JsonToken
import com.google.gson.stream.JsonWriter

/**
 * Custom TypeAdapter for Boolean that can handle different representations:
 * - Boolean values (true/false)
 * - Numeric values (0/1)
 * - String values ("true"/"false" or "1"/"0")
 */
class BooleanTypeAdapter : TypeAdapter<Boolean>() {

    override fun write(out: JsonWriter, value: Boolean?) {
        if (value == null) {
            out.nullValue()
        } else {
            out.value(value)
        }
    }

    override fun read(reader: JsonReader): Boolean? {
        val token = reader.peek()
        return when (token) {
            JsonToken.NULL -> {
                reader.nextNull()
                null
            }
            JsonToken.BOOLEAN -> {
                reader.nextBoolean()
            }
            JsonToken.NUMBER -> {
                reader.nextInt() != 0
            }
            JsonToken.STRING -> {
                val stringValue = reader.nextString()
                when (stringValue.lowercase()) {
                    "true", "1" -> true
                    "false", "0" -> false
                    else -> false
                }
            }
            else -> {
                reader.skipValue()
                null
            }
        }
    }
}
