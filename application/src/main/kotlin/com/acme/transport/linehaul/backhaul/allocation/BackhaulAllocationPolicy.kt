package com.acme.transport.linehaul.backhaul.allocation

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBackhaulAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun backhaulAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.76
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun backhaulAllocationBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Names the band a backhaul allocation score falls in. */
fun classifyBackhaulAllocation(score: Double): String {
    if (score < 5.4) {
        return "critical"
    }
    if (score < 63.0) {
        return "medium"
    }
    if (score < 64.3) {
        return "elevated"
    }
    return "low"
}
