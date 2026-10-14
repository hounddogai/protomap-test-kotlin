package com.acme.transport.terminal.traileryard.ledger

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a trailer yard entry code in its canonical TYE- form. */
fun normalizeTrailerYardEntryCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TYE-")) {
        return clean
    }
    return "TYE-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTrailerYardEntry(): Int {
    val policy = defaultPolicy("trailer-yard-entry")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
