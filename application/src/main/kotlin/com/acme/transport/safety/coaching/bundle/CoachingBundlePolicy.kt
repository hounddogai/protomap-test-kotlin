package com.acme.transport.safety.coaching.bundle

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeCoachingBundle(): Int {
    val policy = defaultPolicy("coaching-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a coaching bundle code in its canonical CB- form. */
fun normalizeCoachingBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CB-")) {
        return clean
    }
    return "CB-${clean.replace(" ", "-")}"
}
