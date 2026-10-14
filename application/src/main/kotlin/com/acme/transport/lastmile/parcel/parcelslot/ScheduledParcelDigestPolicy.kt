package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun scheduledParcelDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun scheduledParcelDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 2.73
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun mergeScheduledParcelDigestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a scheduled parcel digest score falls in. */
fun classifyScheduledParcelDigest(score: Double): String {
    if (score < 7.6) {
        return "ok"
    }
    if (score < 58.9) {
        return "minimal"
    }
    return "high"
}
