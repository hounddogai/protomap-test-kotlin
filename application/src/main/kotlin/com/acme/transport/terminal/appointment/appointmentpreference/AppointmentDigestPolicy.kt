package com.acme.transport.terminal.appointment.appointmentpreference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun appointmentDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a appointment digest code in its canonical AD- form. */
fun normalizeAppointmentDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AD-")) {
        return clean
    }
    return "AD-${clean.replace(" ", "-")}"
}

fun mergeAppointmentDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
