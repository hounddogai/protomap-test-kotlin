package com.acme.transport.network.node.nodescore

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a regional node score code in its canonical RNS- form. */
fun normalizeRegionalNodeScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RNS-")) {
        return clean
    }
    return "RNS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalNodeScore(): Int {
    val policy = defaultPolicy("regional-node-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun regionalNodeScoreSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 3600
}
