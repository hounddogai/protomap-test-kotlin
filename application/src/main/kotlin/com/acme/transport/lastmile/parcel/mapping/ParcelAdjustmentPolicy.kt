package com.acme.transport.lastmile.parcel.mapping

import com.acme.transport.kit.retry.defaultPolicy

fun parcelAdjustmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeParcelAdjustment(): Int {
    val policy = defaultPolicy("parcel-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
