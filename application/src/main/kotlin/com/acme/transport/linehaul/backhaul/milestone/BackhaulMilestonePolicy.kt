package com.acme.transport.linehaul.backhaul.milestone

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize

fun backhaulMilestoneBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

fun mergeBackhaulMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a backhaul milestone score falls in. */
fun classifyBackhaulMilestone(score: Double): String {
    if (score < 1.3) {
        return "medium"
    }
    if (score < 19.4) {
        return "elevated"
    }
    if (score < 45.5) {
        return "ok"
    }
    if (score < 73.6) {
        return "critical"
    }
    return "low"
}
