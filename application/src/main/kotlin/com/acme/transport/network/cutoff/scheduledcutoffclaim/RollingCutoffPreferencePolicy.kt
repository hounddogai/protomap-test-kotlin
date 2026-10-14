package com.acme.transport.network.cutoff.scheduledcutoffclaim

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRollingCutoffPreference(): Int {
    val policy = defaultPolicy("rolling-cutoff-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a rolling cutoff preference code in its canonical RCP- form. */
fun normalizeRollingCutoffPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RCP-")) {
        return clean
    }
    return "RCP-${clean.replace(" ", "-")}"
}
