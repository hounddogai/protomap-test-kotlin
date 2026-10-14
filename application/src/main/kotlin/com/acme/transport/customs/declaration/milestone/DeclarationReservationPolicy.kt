package com.acme.transport.customs.declaration.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationReservation(): Int {
    val policy = defaultPolicy("declaration-reservation")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a declaration reservation code in its canonical DR- form. */
fun normalizeDeclarationReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}
