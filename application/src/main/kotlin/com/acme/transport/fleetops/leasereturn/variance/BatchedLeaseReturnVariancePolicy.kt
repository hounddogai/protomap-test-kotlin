package com.acme.transport.fleetops.leasereturn.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun mergeBatchedLeaseReturnVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun batchedLeaseReturnVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.57
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun batchedLeaseReturnVarianceSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Names the band a batched lease return variance score falls in. */
fun classifyBatchedLeaseReturnVariance(score: Double): String {
    if (score < 33.7) {
        return "watch"
    }
    if (score < 53.1) {
        return "ok"
    }
    if (score < 57.7) {
        return "medium"
    }
    if (score < 63.0) {
        return "critical"
    }
    return "high"
}
