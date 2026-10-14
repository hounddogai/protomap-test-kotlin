package com.acme.transport.crewing.dutyperiod.draft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a duty period manifest score falls in. */
fun classifyDutyPeriodManifest(score: Double): String {
    if (score < 7.8) {
        return "medium"
    }
    if (score < 38.5) {
        return "elevated"
    }
    if (score < 64.2) {
        return "ok"
    }
    return "high"
}

fun dutyPeriodManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

fun mergeDutyPeriodManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
