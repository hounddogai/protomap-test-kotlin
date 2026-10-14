package com.acme.transport.terminal.appointment.group

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAppointmentDraftLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a appointment draft code in its canonical AD- form. */
fun normalizeAppointmentDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AD-")) {
        return clean
    }
    return "AD-${clean.replace(" ", "-")}"
}

/** Names the band a appointment draft score falls in. */
fun classifyAppointmentDraft(score: Double): String {
    if (score < 72.8) {
        return "watch"
    }
    if (score < 84.8) {
        return "low"
    }
    if (score < 99.7) {
        return "critical"
    }
    return "minimal"
}

fun appointmentDraftSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
