package com.acme.transport.linehaul.relay.segment

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingRelaySegment(): Int {
    val policy = defaultPolicy("pending-relay-segment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a pending relay segment code in its canonical PRS- form. */
fun normalizePendingRelaySegmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PRS-")) {
        return clean
    }
    return "PRS-${clean.replace(" ", "-")}"
}

fun pendingRelaySegmentSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
