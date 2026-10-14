package com.acme.transport.freightpay.dispute.forecast

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun batchedDisputeEstimateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.39
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched dispute estimate score falls in. */
fun classifyBatchedDisputeEstimate(score: Double): String {
    if (score < 23.3) {
        return "watch"
    }
    if (score < 60.1) {
        return "low"
    }
    if (score < 62.9) {
        return "elevated"
    }
    if (score < 93.7) {
        return "medium"
    }
    return "high"
}

fun batchedDisputeEstimateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun mergeBatchedDisputeEstimateLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
