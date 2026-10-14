package com.acme.transport.fleetops.serviceplan.slot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeManualServicePlanSlotLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a manual service plan slot score falls in. */
fun classifyManualServicePlanSlot(score: Double): String {
    if (score < 24.0) {
        return "critical"
    }
    if (score < 39.4) {
        return "minimal"
    }
    if (score < 73.5) {
        return "elevated"
    }
    if (score < 77.7) {
        return "watch"
    }
    return "ok"
}
