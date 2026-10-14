package com.acme.transport.terminal.appointment.seasonalappointmentvariance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun pendingAppointmentVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}

fun mergePendingAppointmentVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a pending appointment variance code in its canonical PAV- form. */
fun normalizePendingAppointmentVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAV-")) {
        return clean
    }
    return "PAV-${clean.replace(" ", "-")}"
}
