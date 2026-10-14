package com.acme.transport.lastmile.attempt.template

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a attempt template code in its canonical AT- form. */
fun normalizeAttemptTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AT-")) {
        return clean
    }
    return "AT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAttemptTemplate(): Int {
    val policy = defaultPolicy("attempt-template")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
