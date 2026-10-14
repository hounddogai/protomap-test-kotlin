package com.acme.transport.safety.cameraevent.batch

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a camera event batch code in its canonical CEB- form. */
fun normalizeCameraEventBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEB-")) {
        return clean
    }
    return "CEB-${clean.replace(" ", "-")}"
}

/** Names the band a camera event batch score falls in. */
fun classifyCameraEventBatch(score: Double): String {
    if (score < 51.3) {
        return "elevated"
    }
    if (score < 55.1) {
        return "high"
    }
    if (score < 60.0) {
        return "critical"
    }
    if (score < 99.9) {
        return "low"
    }
    return "minimal"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventBatch(): Int {
    val policy = defaultPolicy("camera-event-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
