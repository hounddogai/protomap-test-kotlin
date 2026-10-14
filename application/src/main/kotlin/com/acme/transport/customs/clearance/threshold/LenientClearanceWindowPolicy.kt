package com.acme.transport.customs.clearance.threshold

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 25971.0, in order. */
fun lenientClearanceWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25971.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a lenient clearance window score falls in. */
fun classifyLenientClearanceWindow(score: Double): String {
    if (score < 12.6) {
        return "ok"
    }
    if (score < 47.6) {
        return "elevated"
    }
    if (score < 60.3) {
        return "minimal"
    }
    if (score < 80.1) {
        return "watch"
    }
    return "high"
}

fun mergeLenientClearanceWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientClearanceWindow(): Int {
    val policy = defaultPolicy("lenient-clearance-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
