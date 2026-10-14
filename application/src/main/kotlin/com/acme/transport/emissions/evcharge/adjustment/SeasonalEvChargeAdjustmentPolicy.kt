package com.acme.transport.emissions.evcharge.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeSeasonalEvChargeAdjustmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a seasonal ev charge adjustment score falls in. */
fun classifySeasonalEvChargeAdjustment(score: Double): String {
    if (score < 83.9) {
        return "critical"
    }
    if (score < 92.1) {
        return "ok"
    }
    return "low"
}
