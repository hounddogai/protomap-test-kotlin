package com.acme.transport.crewing.availability.segment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryAvailabilityBaseline(): Int {
    val policy = defaultPolicy("primary-availability-baseline")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergePrimaryAvailabilityBaselineLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryAvailabilityBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.34
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a primary availability baseline score falls in. */
fun classifyPrimaryAvailabilityBaseline(score: Double): String {
    if (score < 46.4) {
        return "high"
    }
    if (score < 57.0) {
        return "elevated"
    }
    if (score < 63.5) {
        return "watch"
    }
    return "low"
}
