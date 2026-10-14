package com.acme.transport.safety.cameraevent.eventbundle

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a camera event threshold code in its canonical CET- form. */
fun normalizeCameraEventThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CET-")) {
        return clean
    }
    return "CET-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventThreshold(): Int {
    val policy = defaultPolicy("camera-event-threshold")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
