package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a weighted camera event assignment code in its canonical WCEA- form. */
fun normalizeWeightedCameraEventAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WCEA-")) {
        return clean
    }
    return "WCEA-${clean.replace(" ", "-")}"
}

fun mergeWeightedCameraEventAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
