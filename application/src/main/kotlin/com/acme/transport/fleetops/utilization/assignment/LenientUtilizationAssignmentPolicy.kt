package com.acme.transport.fleetops.utilization.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a lenient utilization assignment code in its canonical LUA- form. */
fun normalizeLenientUtilizationAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LUA-")) {
        return clean
    }
    return "LUA-${clean.replace(" ", "-")}"
}

fun mergeLenientUtilizationAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
