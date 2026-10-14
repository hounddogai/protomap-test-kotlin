package com.acme.transport.network.zone.profile

import com.acme.transport.kit.retry.defaultPolicy

fun globalZoneTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Puts a global zone template code in its canonical GZT- form. */
fun normalizeGlobalZoneTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GZT-")) {
        return clean
    }
    return "GZT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeGlobalZoneTemplate(): Int {
    val policy = defaultPolicy("global-zone-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
