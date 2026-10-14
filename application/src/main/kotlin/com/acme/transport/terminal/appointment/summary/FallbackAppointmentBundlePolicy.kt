package com.acme.transport.terminal.appointment.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun fallbackAppointmentBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Puts a fallback appointment bundle code in its canonical FAB- form. */
fun normalizeFallbackAppointmentBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FAB-")) {
        return clean
    }
    return "FAB-${clean.replace(" ", "-")}"
}

fun mergeFallbackAppointmentBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
