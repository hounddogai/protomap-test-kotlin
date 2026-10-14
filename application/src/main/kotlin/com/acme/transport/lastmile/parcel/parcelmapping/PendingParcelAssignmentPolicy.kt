package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.kit.retry.defaultPolicy

fun pendingParcelAssignmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingParcelAssignment(): Int {
    val policy = defaultPolicy("pending-parcel-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
