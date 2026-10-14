package com.acme.transport.network.node.nodedraft

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.text.normalize

/** Names the band a primary node milestone score falls in. */
fun classifyPrimaryNodeMilestone(score: Double): String {
    if (score < 49.9) {
        return "elevated"
    }
    if (score < 86.7) {
        return "critical"
    }
    return "minimal"
}

fun primaryNodeMilestoneSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}

/** Counts how many amounts fit in a budget of 5747.0, in order. */
fun primaryNodeMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 5747.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergePrimaryNodeMilestoneLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
