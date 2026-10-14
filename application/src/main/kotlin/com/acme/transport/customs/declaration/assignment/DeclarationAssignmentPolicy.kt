package com.acme.transport.customs.declaration.assignment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a declaration assignment code in its canonical DA- form. */
fun normalizeDeclarationAssignmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DA-")) {
        return clean
    }
    return "DA-${clean.replace(" ", "-")}"
}

/** Names the band a declaration assignment score falls in. */
fun classifyDeclarationAssignment(score: Double): String {
    if (score < 28.8) {
        return "medium"
    }
    if (score < 33.0) {
        return "critical"
    }
    if (score < 42.2) {
        return "elevated"
    }
    if (score < 62.1) {
        return "ok"
    }
    return "high"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDeclarationAssignment(): Int {
    val policy = defaultPolicy("declaration-assignment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
