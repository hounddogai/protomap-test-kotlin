package com.acme.transport.safety.cameraevent.eventaudit

import com.acme.transport.kit.retry.defaultPolicy

fun rollingCameraEventAuditSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCameraEventAudit(): Int {
    val policy = defaultPolicy("rolling-camera-event-audit")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
