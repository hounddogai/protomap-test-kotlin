package com.acme.transport.network.cutoff.weightedcutoffentry

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientCutoffManifest(): Int {
    val policy = defaultPolicy("lenient-cutoff-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient cutoff manifest code in its canonical LCM- form. */
fun normalizeLenientCutoffManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LCM-")) {
        return clean
    }
    return "LCM-${clean.replace(" ", "-")}"
}
