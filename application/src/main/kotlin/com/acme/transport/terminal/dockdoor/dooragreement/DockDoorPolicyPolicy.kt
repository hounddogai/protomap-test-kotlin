package com.acme.transport.terminal.dockdoor.dooragreement

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a dock door policy code in its canonical DDP- form. */
fun normalizeDockDoorPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDP-")) {
        return clean
    }
    return "DDP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeDockDoorPolicy(): Int {
    val policy = defaultPolicy("dock-door-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun dockDoorPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a dock door policy score falls in. */
fun classifyDockDoorPolicy(score: Double): String {
    if (score < 22.3) {
        return "elevated"
    }
    if (score < 98.5) {
        return "medium"
    }
    return "minimal"
}
