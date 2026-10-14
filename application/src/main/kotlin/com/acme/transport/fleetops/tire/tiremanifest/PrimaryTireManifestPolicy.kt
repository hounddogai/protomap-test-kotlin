package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryTireManifest(): Int {
    val policy = defaultPolicy("primary-tire-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 17066.0, in order. */
fun primaryTireManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17066.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a primary tire manifest score falls in. */
fun classifyPrimaryTireManifest(score: Double): String {
    if (score < 52.3) {
        return "high"
    }
    if (score < 71.0) {
        return "low"
    }
    if (score < 72.3) {
        return "elevated"
    }
    return "medium"
}

fun mergePrimaryTireManifestLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
