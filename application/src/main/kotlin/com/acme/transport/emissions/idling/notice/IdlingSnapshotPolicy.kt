package com.acme.transport.emissions.idling.notice

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a idling snapshot code in its canonical IS- form. */
fun normalizeIdlingSnapshotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("IS-")) {
        return clean
    }
    return "IS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeIdlingSnapshot(): Int {
    val policy = defaultPolicy("idling-snapshot")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
