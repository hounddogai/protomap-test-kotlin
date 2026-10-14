package com.acme.transport.customs.clearance.target

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryClearanceGroup(): Int {
    val policy = defaultPolicy("primary-clearance-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a primary clearance group score falls in. */
fun classifyPrimaryClearanceGroup(score: Double): String {
    if (score < 29.9) {
        return "high"
    }
    if (score < 83.0) {
        return "low"
    }
    if (score < 94.3) {
        return "critical"
    }
    if (score < 99.7) {
        return "medium"
    }
    return "ok"
}

fun mergePrimaryClearanceGroupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun primaryClearanceGroupWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
