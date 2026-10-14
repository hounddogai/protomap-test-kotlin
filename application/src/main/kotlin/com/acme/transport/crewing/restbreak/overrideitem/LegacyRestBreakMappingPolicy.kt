package com.acme.transport.crewing.restbreak.overrideitem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun legacyRestBreakMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a legacy rest break mapping code in its canonical LRBM- form. */
fun normalizeLegacyRestBreakMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRBM-")) {
        return clean
    }
    return "LRBM-${clean.replace(" ", "-")}"
}

fun mergeLegacyRestBreakMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
