package com.acme.transport.fleetops.inspection.agreement

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a inspection notice score falls in. */
fun classifyInspectionNotice(score: Double): String {
    if (score < 1.0) {
        return "minimal"
    }
    if (score < 2.8) {
        return "critical"
    }
    if (score < 40.8) {
        return "low"
    }
    if (score < 99.7) {
        return "ok"
    }
    return "elevated"
}

fun inspectionNoticeSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

fun mergeInspectionNoticeLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
