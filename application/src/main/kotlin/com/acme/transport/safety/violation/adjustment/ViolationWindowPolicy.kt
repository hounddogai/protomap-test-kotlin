package com.acme.transport.safety.violation.adjustment

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeViolationWindow(): Int {
    val policy = defaultPolicy("violation-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a violation window score falls in. */
fun classifyViolationWindow(score: Double): String {
    if (score < 27.2) {
        return "watch"
    }
    if (score < 40.2) {
        return "elevated"
    }
    return "low"
}

fun mergeViolationWindowLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 6519.0, in order. */
fun violationWindowBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 6519.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
