package com.acme.transport.customs.hold.slot

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a hold manifest code in its canonical HM- form. */
fun normalizeHoldManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("HM-")) {
        return clean
    }
    return "HM-${clean.replace(" ", "-")}"
}

fun holdManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeHoldManifest(): Int {
    val policy = defaultPolicy("hold-manifest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a hold manifest score falls in. */
fun classifyHoldManifest(score: Double): String {
    if (score < 37.7) {
        return "critical"
    }
    if (score < 82.8) {
        return "ok"
    }
    if (score < 95.2) {
        return "elevated"
    }
    return "minimal"
}
