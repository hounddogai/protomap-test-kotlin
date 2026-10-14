package com.acme.transport.crewing.swap.request

import com.acme.transport.kit.numbers.roundTo

fun lenientSwapBatchBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a lenient swap batch score falls in. */
fun classifyLenientSwapBatch(score: Double): String {
    if (score < 54.6) {
        return "minimal"
    }
    if (score < 70.7) {
        return "elevated"
    }
    return "watch"
}
