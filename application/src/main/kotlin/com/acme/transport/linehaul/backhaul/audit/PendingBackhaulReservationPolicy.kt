package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingBackhaulReservationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending backhaul reservation score falls in. */
fun classifyPendingBackhaulReservation(score: Double): String {
    if (score < 35.6) {
        return "low"
    }
    if (score < 41.3) {
        return "minimal"
    }
    if (score < 58.6) {
        return "critical"
    }
    return "ok"
}
