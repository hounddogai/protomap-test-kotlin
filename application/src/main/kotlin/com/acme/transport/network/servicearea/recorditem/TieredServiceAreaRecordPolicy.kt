package com.acme.transport.network.servicearea.recorditem

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Names the band a tiered service area record score falls in. */
fun classifyTieredServiceAreaRecord(score: Double): String {
    if (score < 20.4) {
        return "critical"
    }
    if (score < 43.2) {
        return "ok"
    }
    if (score < 65.0) {
        return "medium"
    }
    if (score < 76.2) {
        return "minimal"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredServiceAreaRecord(): Int {
    val policy = defaultPolicy("tiered-service-area-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeTieredServiceAreaRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Counts how many amounts fit in a budget of 91106.0, in order. */
fun tieredServiceAreaRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 91106.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
