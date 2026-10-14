package com.acme.transport.network.cutoff.quota

import com.acme.transport.kit.numbers.roundTo

fun cappedCutoffManifestBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a capped cutoff manifest score falls in. */
fun classifyCappedCutoffManifest(score: Double): String {
    if (score < 91.4) {
        return "high"
    }
    if (score < 98.5) {
        return "medium"
    }
    return "minimal"
}

fun cappedCutoffManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}
