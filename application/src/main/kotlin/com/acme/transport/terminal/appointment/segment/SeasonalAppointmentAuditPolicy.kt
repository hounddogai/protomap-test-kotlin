package com.acme.transport.terminal.appointment.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a seasonal appointment audit score falls in. */
fun classifySeasonalAppointmentAudit(score: Double): String {
    if (score < 28.6) {
        return "elevated"
    }
    if (score < 57.4) {
        return "ok"
    }
    return "medium"
}

fun seasonalAppointmentAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun mergeSeasonalAppointmentAuditLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
