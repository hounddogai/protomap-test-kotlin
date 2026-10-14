package com.acme.transport.terminal.dockdoor.proposal

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a dock door tier code in its canonical DDT- form. */
fun normalizeDockDoorTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDT-")) {
        return clean
    }
    return "DDT-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorTier(): Int {
    val policy = defaultPolicy("dock-door-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dockDoorTierWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.43
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a dock door tier score falls in. */
fun classifyDockDoorTier(score: Double): String {
    if (score < 24.9) {
        return "medium"
    }
    if (score < 69.1) {
        return "ok"
    }
    return "minimal"
}
