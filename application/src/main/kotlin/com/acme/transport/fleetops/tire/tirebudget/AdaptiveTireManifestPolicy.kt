package com.acme.transport.fleetops.tire.tirebudget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a adaptive tire manifest score falls in. */
fun classifyAdaptiveTireManifest(score: Double): String {
    if (score < 18.3) {
        return "ok"
    }
    if (score < 22.2) {
        return "minimal"
    }
    if (score < 81.7) {
        return "high"
    }
    return "low"
}

/** Counts how many amounts fit in a budget of 46728.0, in order. */
fun adaptiveTireManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46728.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeAdaptiveTireManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveTireManifest(): Int {
    val policy = defaultPolicy("adaptive-tire-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
