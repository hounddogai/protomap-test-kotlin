package com.acme.transport.terminal.traileryard.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyTrailerYardManifest(): Int {
    val policy = defaultPolicy("legacy-trailer-yard-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyTrailerYardManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a legacy trailer yard manifest code in its canonical LTYM- form. */
fun normalizeLegacyTrailerYardManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LTYM-")) {
        return clean
    }
    return "LTYM-${clean.replace(" ", "-")}"
}
