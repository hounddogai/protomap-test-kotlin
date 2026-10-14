package com.acme.transport.safety.nearmiss.rule

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNearMissRule(): Int {
    val policy = defaultPolicy("near-miss-rule")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a near miss rule code in its canonical NMR- form. */
fun normalizeNearMissRuleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("NMR-")) {
        return clean
    }
    return "NMR-${clean.replace(" ", "-")}"
}

fun nearMissRuleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}
