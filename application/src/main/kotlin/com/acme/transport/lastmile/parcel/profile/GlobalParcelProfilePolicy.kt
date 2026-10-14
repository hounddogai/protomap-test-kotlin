package com.acme.transport.lastmile.parcel.profile

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun globalParcelProfileSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a global parcel profile code in its canonical GPP- form. */
fun normalizeGlobalParcelProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GPP-")) {
        return clean
    }
    return "GPP-${clean.replace(" ", "-")}"
}

fun mergeGlobalParcelProfileLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
