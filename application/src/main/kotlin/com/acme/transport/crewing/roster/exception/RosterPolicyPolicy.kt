package com.acme.transport.crewing.roster.exception

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

fun mergeRosterPolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a roster policy score falls in. */
fun classifyRosterPolicy(score: Double): String {
    if (score < 33.3) {
        return "critical"
    }
    if (score < 80.2) {
        return "low"
    }
    return "elevated"
}
