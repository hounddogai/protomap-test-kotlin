package com.acme.transport.customs.broker.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a weighted broker score code in its canonical WBS- form. */
fun normalizeWeightedBrokerScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WBS-")) {
        return clean
    }
    return "WBS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedBrokerScore(): Int {
    val policy = defaultPolicy("weighted-broker-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
