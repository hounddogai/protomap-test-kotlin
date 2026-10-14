package com.acme.transport.network.servicearea.areaclaim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeWeightedServiceAreaCheckpoint(): Int {
    val policy = defaultPolicy("weighted-service-area-checkpoint")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a weighted service area checkpoint code in its canonical WSAC- form. */
fun normalizeWeightedServiceAreaCheckpointCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WSAC-")) {
        return clean
    }
    return "WSAC-${clean.replace(" ", "-")}"
}
