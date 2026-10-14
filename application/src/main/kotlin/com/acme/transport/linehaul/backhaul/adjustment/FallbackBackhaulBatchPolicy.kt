package com.acme.transport.linehaul.backhaul.adjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a fallback backhaul batch code in its canonical FBB- form. */
fun normalizeFallbackBackhaulBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBB-")) {
        return clean
    }
    return "FBB-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackBackhaulBatch(): Int {
    val policy = defaultPolicy("fallback-backhaul-batch")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
