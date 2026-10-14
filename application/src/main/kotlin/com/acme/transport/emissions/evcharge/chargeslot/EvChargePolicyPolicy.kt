package com.acme.transport.emissions.evcharge.chargeslot

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Counts how many amounts fit in a budget of 46934.0, in order. */
fun evChargePolicyBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46934.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeEvChargePolicyLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeEvChargePolicy(): Int {
    val policy = defaultPolicy("ev-charge-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a ev charge policy score falls in. */
fun classifyEvChargePolicy(score: Double): String {
    if (score < 6.1) {
        return "ok"
    }
    if (score < 59.1) {
        return "watch"
    }
    if (score < 64.2) {
        return "high"
    }
    return "minimal"
}
