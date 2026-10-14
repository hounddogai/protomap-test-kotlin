package com.acme.transport.linehaul.relay.schedule

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a manual relay assignment score falls in. */
fun classifyManualRelayAssignment(score: Double): String {
    if (score < 22.2) {
        return "ok"
    }
    if (score < 32.1) {
        return "critical"
    }
    if (score < 78.6) {
        return "minimal"
    }
    return "elevated"
}

fun mergeManualRelayAssignmentLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualRelayAssignment(): Int {
    val policy = defaultPolicy("manual-relay-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 41406.0, in order. */
fun manualRelayAssignmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41406.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
