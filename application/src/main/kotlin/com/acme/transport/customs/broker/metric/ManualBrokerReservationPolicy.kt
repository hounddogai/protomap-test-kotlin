package com.acme.transport.customs.broker.metric

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualBrokerReservation(): Int {
    val policy = defaultPolicy("manual-broker-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a manual broker reservation code in its canonical MBR- form. */
fun normalizeManualBrokerReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MBR-")) {
        return clean
    }
    return "MBR-${clean.replace(" ", "-")}"
}
