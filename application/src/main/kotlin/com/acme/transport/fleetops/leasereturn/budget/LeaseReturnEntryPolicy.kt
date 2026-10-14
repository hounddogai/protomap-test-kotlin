package com.acme.transport.fleetops.leasereturn.budget

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import com.acme.transport.kit.text.normalize

fun leaseReturnEntryBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

fun mergeLeaseReturnEntryLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLeaseReturnEntry(): Int {
    val policy = defaultPolicy("lease-return-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
