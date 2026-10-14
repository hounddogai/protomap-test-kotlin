package com.acme.transport.lastmile.locker.preference

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun lockerExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.54
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun lockerExceptionBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun mergeLockerExceptionLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a locker exception score falls in. */
fun classifyLockerException(score: Double): String {
    if (score < 31.8) {
        return "critical"
    }
    if (score < 40.5) {
        return "watch"
    }
    if (score < 64.7) {
        return "high"
    }
    if (score < 91.0) {
        return "medium"
    }
    return "minimal"
}
