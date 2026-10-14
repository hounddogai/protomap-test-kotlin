package com.acme.transport.fleetops.leasereturn.request

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnAllocation(): Int {
    val policy = defaultPolicy("lease-return-allocation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun mergeLeaseReturnAllocationLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Names the band a lease return allocation score falls in. */
fun classifyLeaseReturnAllocation(score: Double): String {
    if (score < 25.0) {
        return "critical"
    }
    if (score < 37.4) {
        return "low"
    }
    if (score < 67.3) {
        return "high"
    }
    if (score < 70.2) {
        return "ok"
    }
    return "watch"
}
