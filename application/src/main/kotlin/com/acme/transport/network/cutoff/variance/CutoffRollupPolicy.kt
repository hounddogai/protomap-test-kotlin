package com.acme.transport.network.cutoff.variance

import com.acme.transport.kit.numbers.roundTo

fun cutoffRollupBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Names the band a cutoff rollup score falls in. */
fun classifyCutoffRollup(score: Double): String {
    if (score < 28.5) {
        return "minimal"
    }
    if (score < 70.4) {
        return "low"
    }
    return "elevated"
}
