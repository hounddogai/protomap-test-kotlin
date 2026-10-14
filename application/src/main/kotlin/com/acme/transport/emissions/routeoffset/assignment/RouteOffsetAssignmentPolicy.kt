package com.acme.transport.emissions.routeoffset.assignment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun routeOffsetAssignmentWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a route offset assignment score falls in. */
fun classifyRouteOffsetAssignment(score: Double): String {
    if (score < 51.2) {
        return "medium"
    }
    if (score < 54.2) {
        return "ok"
    }
    if (score < 83.3) {
        return "critical"
    }
    return "low"
}

fun mergeRouteOffsetAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun routeOffsetAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
