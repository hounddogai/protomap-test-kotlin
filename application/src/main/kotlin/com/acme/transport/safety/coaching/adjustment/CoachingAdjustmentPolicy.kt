package com.acme.transport.safety.coaching.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a coaching adjustment code in its canonical CA- form. */
fun normalizeCoachingAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CA-")) {
        return clean
    }
    return "CA-${clean.replace(" ", "-")}"
}

fun mergeCoachingAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
