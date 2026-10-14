package com.acme.transport.crewing.swap.bundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun batchedSwapBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a batched swap bundle score falls in. */
fun classifyBatchedSwapBundle(score: Double): String {
    if (score < 47.4) {
        return "high"
    }
    if (score < 83.7) {
        return "ok"
    }
    return "watch"
}
