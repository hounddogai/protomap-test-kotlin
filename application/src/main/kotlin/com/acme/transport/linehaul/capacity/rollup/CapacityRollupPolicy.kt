package com.acme.transport.linehaul.capacity.rollup

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCapacityRollup(): Int {
    val policy = defaultPolicy("capacity-rollup")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 10796.0, in order. */
fun capacityRollupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 10796.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capacity rollup score falls in. */
fun classifyCapacityRollup(score: Double): String {
    if (score < 20.9) {
        return "minimal"
    }
    if (score < 73.7) {
        return "critical"
    }
    if (score < 82.0) {
        return "ok"
    }
    if (score < 83.7) {
        return "watch"
    }
    return "medium"
}

fun mergeCapacityRollupLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
