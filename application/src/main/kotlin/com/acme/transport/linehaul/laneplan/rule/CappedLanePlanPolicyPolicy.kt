package com.acme.transport.linehaul.laneplan.rule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeCappedLanePlanPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun cappedLanePlanPolicySpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a capped lane plan policy score falls in. */
fun classifyCappedLanePlanPolicy(score: Double): String {
    if (score < 5.9) {
        return "minimal"
    }
    if (score < 71.4) {
        return "ok"
    }
    if (score < 82.6) {
        return "critical"
    }
    return "watch"
}
