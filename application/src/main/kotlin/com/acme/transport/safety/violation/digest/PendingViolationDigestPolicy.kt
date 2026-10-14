package com.acme.transport.safety.violation.digest

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a pending violation digest code in its canonical PVD- form. */
fun normalizePendingViolationDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PVD-")) {
        return clean
    }
    return "PVD-${clean.replace(" ", "-")}"
}

fun pendingViolationDigestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePendingViolationDigest(): Int {
    val policy = defaultPolicy("pending-violation-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
