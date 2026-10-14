package com.acme.transport.linehaul.laneplan.planbundle

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun manualLanePlanBundleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

fun manualLanePlanBundleBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeManualLanePlanBundleLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a manual lane plan bundle score falls in. */
fun classifyManualLanePlanBundle(score: Double): String {
    if (score < 4.2) {
        return "elevated"
    }
    if (score < 31.3) {
        return "minimal"
    }
    if (score < 74.7) {
        return "ok"
    }
    return "critical"
}
