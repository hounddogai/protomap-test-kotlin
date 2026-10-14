package com.acme.transport.terminal.appointment.metric

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeWeightedAppointmentMetricLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Puts a weighted appointment metric code in its canonical WAM- form. */
fun normalizeWeightedAppointmentMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WAM-")) {
        return clean
    }
    return "WAM-${clean.replace(" ", "-")}"
}

/** Names the band a weighted appointment metric score falls in. */
fun classifyWeightedAppointmentMetric(score: Double): String {
    if (score < 14.3) {
        return "low"
    }
    if (score < 42.9) {
        return "minimal"
    }
    if (score < 48.4) {
        return "high"
    }
    return "medium"
}
