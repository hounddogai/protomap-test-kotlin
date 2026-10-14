package com.acme.transport.freightpay.dispute.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Names the band a default dispute snapshot score falls in. */
fun classifyDefaultDisputeSnapshot(score: Double): String {
    if (score < 81.1) {
        return "elevated"
    }
    if (score < 83.0) {
        return "ok"
    }
    return "medium"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDefaultDisputeSnapshot(): Int {
    val policy = defaultPolicy("default-dispute-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a default dispute snapshot code in its canonical DDS- form. */
fun normalizeDefaultDisputeSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDS-")) {
        return clean
    }
    return "DDS-${clean.replace(" ", "-")}"
}
