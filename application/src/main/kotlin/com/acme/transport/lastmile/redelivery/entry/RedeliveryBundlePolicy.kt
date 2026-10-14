package com.acme.transport.lastmile.redelivery.entry

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRedeliveryBundle(): Int {
    val policy = defaultPolicy("redelivery-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a redelivery bundle code in its canonical RB- form. */
fun normalizeRedeliveryBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RB-")) {
        return clean
    }
    return "RB-${clean.replace(" ", "-")}"
}
