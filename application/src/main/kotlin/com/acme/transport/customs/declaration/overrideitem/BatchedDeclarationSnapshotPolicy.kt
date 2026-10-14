package com.acme.transport.customs.declaration.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeBatchedDeclarationSnapshotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a batched declaration snapshot code in its canonical BDS- form. */
fun normalizeBatchedDeclarationSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BDS-")) {
        return clean
    }
    return "BDS-${clean.replace(" ", "-")}"
}
