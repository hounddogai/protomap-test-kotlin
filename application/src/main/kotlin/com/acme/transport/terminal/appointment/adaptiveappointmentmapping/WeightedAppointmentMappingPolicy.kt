package com.acme.transport.terminal.appointment.adaptiveappointmentmapping

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun weightedAppointmentMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

/** Names the band a weighted appointment mapping score falls in. */
fun classifyWeightedAppointmentMapping(score: Double): String {
    if (score < 46.7) {
        return "minimal"
    }
    if (score < 90.4) {
        return "watch"
    }
    return "high"
}

fun weightedAppointmentMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeWeightedAppointmentMappingLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
