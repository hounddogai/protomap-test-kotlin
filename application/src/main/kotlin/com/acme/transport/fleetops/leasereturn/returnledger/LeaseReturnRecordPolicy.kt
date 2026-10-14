package com.acme.transport.fleetops.leasereturn.returnledger

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnRecord(): Int {
    val policy = defaultPolicy("lease-return-record")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 78525.0, in order. */
fun leaseReturnRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 78525.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeLeaseReturnRecordLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lease return record score falls in. */
fun classifyLeaseReturnRecord(score: Double): String {
    if (score < 5.1) {
        return "elevated"
    }
    if (score < 18.3) {
        return "critical"
    }
    if (score < 28.1) {
        return "minimal"
    }
    if (score < 98.4) {
        return "medium"
    }
    return "high"
}
