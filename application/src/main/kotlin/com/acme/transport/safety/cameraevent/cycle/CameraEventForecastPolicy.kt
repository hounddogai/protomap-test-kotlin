package com.acme.transport.safety.cameraevent.cycle

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a camera event forecast code in its canonical CEF- form. */
fun normalizeCameraEventForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CEF-")) {
        return clean
    }
    return "CEF-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCameraEventForecast(): Int {
    val policy = defaultPolicy("camera-event-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
