package com.acme.transport.network.node.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted node window code in its canonical WNW- form. */
fun normalizeWeightedNodeWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WNW-")) {
        return clean
    }
    return "WNW-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedNodeWindow(): Int {
    val policy = defaultPolicy("weighted-node-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
