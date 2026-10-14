package com.acme.transport.lastmile.parcel.reservation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a parcel metric code in its canonical PM- form. */
fun normalizeParcelMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PM-")) {
        return clean
    }
    return "PM-${clean.replace(" ", "-")}"
}

fun mergeParcelMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
