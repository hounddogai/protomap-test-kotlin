package com.acme.transport.linehaul.backhaul.backhaultarget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergePendingBackhaulForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a pending backhaul forecast score falls in. */
fun classifyPendingBackhaulForecast(score: Double): String {
    if (score < 12.6) {
        return "elevated"
    }
    if (score < 35.8) {
        return "ok"
    }
    if (score < 62.8) {
        return "critical"
    }
    if (score < 68.3) {
        return "minimal"
    }
    return "watch"
}

fun pendingBackhaulForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
