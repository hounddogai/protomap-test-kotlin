package com.acme.transport.terminal.appointment.entry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Names the band a scheduled appointment summary score falls in. */
fun classifyScheduledAppointmentSummary(score: Double): String {
    if (score < 34.1) {
        return "ok"
    }
    if (score < 55.4) {
        return "watch"
    }
    if (score < 61.3) {
        return "medium"
    }
    return "critical"
}

fun mergeScheduledAppointmentSummaryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun scheduledAppointmentSummaryBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun scheduledAppointmentSummaryWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.33
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
