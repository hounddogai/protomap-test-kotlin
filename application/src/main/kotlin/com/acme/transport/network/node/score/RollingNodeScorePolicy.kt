package com.acme.transport.network.node.score

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingNodeScore(): Int {
    val policy = defaultPolicy("rolling-node-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun rollingNodeScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 86400
}

/** Puts a rolling node score code in its canonical RNS- form. */
fun normalizeRollingNodeScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNS-")) {
        return clean
    }
    return "RNS-${clean.replace(" ", "-")}"
}
