package com.acme.transport.safety.nearmiss.agreement

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fallback near miss budget code in its canonical FNMB- form. */
fun normalizeFallbackNearMissBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FNMB-")) {
        return clean
    }
    return "FNMB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackNearMissBudget(): Int {
    val policy = defaultPolicy("fallback-near-miss-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
