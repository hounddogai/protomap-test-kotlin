package com.acme.transport.crewing.restbreak.regionalbreakpreference

import com.acme.transport.kit.retry.defaultPolicy

fun defaultRestBreakScheduleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultRestBreakSchedule(): Int {
    val policy = defaultPolicy("default-rest-break-schedule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
