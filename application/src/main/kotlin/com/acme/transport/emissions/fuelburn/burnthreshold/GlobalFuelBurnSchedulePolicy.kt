package com.acme.transport.emissions.fuelburn.burnthreshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a global fuel burn schedule score falls in. */
fun classifyGlobalFuelBurnSchedule(score: Double): String {
    if (score < 12.3) {
        return "critical"
    }
    if (score < 47.1) {
        return "elevated"
    }
    if (score < 65.7) {
        return "medium"
    }
    return "low"
}

fun mergeGlobalFuelBurnScheduleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
