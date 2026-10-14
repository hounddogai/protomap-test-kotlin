package com.acme.transport.freightpay.dispute.manifest

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun defaultDisputeManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

fun defaultDisputeManifestBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

fun defaultDisputeManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a default dispute manifest score falls in. */
fun classifyDefaultDisputeManifest(score: Double): String {
    if (score < 7.9) {
        return "minimal"
    }
    if (score < 9.2) {
        return "high"
    }
    if (score < 38.9) {
        return "medium"
    }
    if (score < 77.6) {
        return "ok"
    }
    return "watch"
}
