package com.acme.transport.terminal.appointment.estimate

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a appointment review score falls in. */
fun classifyAppointmentReview(score: Double): String {
    if (score < 22.7) {
        return "minimal"
    }
    if (score < 73.7) {
        return "low"
    }
    if (score < 84.0) {
        return "medium"
    }
    return "critical"
}

fun mergeAppointmentReviewLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
