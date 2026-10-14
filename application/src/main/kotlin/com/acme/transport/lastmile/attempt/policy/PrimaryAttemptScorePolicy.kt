package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a primary attempt score code in its canonical PAS- form. */
fun normalizePrimaryAttemptScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PAS-")) {
        return clean
    }
    return "PAS-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryAttemptScore(): Int {
    val policy = defaultPolicy("primary-attempt-score")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
