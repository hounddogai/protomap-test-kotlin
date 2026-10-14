package com.acme.transport.lastmile.drop.rollup

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun regionalDropTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.46
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeRegionalDropTier(): Int {
    val policy = defaultPolicy("regional-drop-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Names the band a regional drop tier score falls in. */
fun classifyRegionalDropTier(score: Double): String {
    if (score < 73.8) {
        return "medium"
    }
    if (score < 78.9) {
        return "ok"
    }
    if (score < 84.6) {
        return "minimal"
    }
    return "watch"
}

/** Puts a regional drop tier code in its canonical RDT- form. */
fun normalizeRegionalDropTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDT-")) {
        return clean
    }
    return "RDT-${clean.replace(" ", "-")}"
}
