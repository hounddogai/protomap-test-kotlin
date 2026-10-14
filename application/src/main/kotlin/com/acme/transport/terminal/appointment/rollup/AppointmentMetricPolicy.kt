package com.acme.transport.terminal.appointment.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeAppointmentMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a appointment metric score falls in. */
fun classifyAppointmentMetric(score: Double): String {
    if (score < 40.7) {
        return "ok"
    }
    if (score < 60.5) {
        return "watch"
    }
    return "low"
}

/** Puts a appointment metric code in its canonical AM- form. */
fun normalizeAppointmentMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AM-")) {
        return clean
    }
    return "AM-${clean.replace(" ", "-")}"
}
