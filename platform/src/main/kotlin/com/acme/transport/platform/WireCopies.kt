package com.acme.transport.platform

import com.google.protobuf.Message
import com.google.protobuf.util.JsonFormat

/** Interoperability adapters for partners that still exchange JSON or select message types at runtime. */
object WireCopies {
    fun <T : Message> json(source: T): T {
        val builder = source.newBuilderForType()
        JsonFormat.parser().merge(JsonFormat.printer().print(source), builder)
        return typed(source, builder.build())
    }

    fun <T : Message> reflective(source: T): T {
        val builder = source.javaClass.getMethod("newBuilder").invoke(null) as Message.Builder
        source.allFields.entries.sortedBy { it.key.number }.forEach { (field, value) ->
            builder.setField(builder.descriptorForType.findFieldByNumber(field.number), value)
        }
        return typed(source, builder.build())
    }

    @Suppress("UNCHECKED_CAST")
    private fun <T : Message> typed(source: T, value: Message): T = source.javaClass.cast(value) as T
}
