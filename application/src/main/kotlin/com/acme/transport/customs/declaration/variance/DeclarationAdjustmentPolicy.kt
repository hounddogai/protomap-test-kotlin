package com.acme.transport.customs.declaration.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeDeclarationAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a declaration adjustment code in its canonical DA- form. */
fun normalizeDeclarationAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}
