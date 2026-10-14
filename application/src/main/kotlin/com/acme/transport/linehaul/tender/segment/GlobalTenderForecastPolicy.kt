package com.acme.transport.linehaul.tender.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeGlobalTenderForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a global tender forecast score falls in. */
fun classifyGlobalTenderForecast(score: Double): String {
    if (score < 31.7) {
        return "high"
    }
    if (score < 70.8) {
        return "critical"
    }
    return "ok"
}
