package com.acme.transport.emissions.idling.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCappedIdlingWindow(): Int {
    val policy = defaultPolicy("capped-idling-window")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a capped idling window code in its canonical CIW- form. */
fun normalizeCappedIdlingWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CIW-")) {
        return clean
    }
    return "CIW-${clean.replace(" ", "-")}"
}
