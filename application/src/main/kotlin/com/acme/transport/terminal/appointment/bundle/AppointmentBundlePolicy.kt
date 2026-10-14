package com.acme.transport.terminal.appointment.bundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Puts a appointment bundle code in its canonical AB- form. */
fun normalizeAppointmentBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AB-")) {
        return clean
    }
    return "AB-${clean.replace(" ", "-")}"
}

/** Names the band a appointment bundle score falls in. */
fun classifyAppointmentBundle(score: Double): String {
    if (score < 96.5) {
        return "medium"
    }
    if (score < 96.7) {
        return "low"
    }
    return "critical"
}

fun appointmentBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

fun mergeAppointmentBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
