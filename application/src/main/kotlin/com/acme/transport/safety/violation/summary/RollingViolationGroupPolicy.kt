package com.acme.transport.safety.violation.summary

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRollingViolationGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a rolling violation group score falls in. */
fun classifyRollingViolationGroup(score: Double): String {
    if (score < 10.6) {
        return "minimal"
    }
    if (score < 29.6) {
        return "low"
    }
    if (score < 55.9) {
        return "elevated"
    }
    if (score < 87.7) {
        return "ok"
    }
    return "watch"
}
