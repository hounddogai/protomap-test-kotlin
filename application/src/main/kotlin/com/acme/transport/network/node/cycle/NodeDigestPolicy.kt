package com.acme.transport.network.node.cycle

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeNodeDigest(): Int {
    val policy = defaultPolicy("node-digest")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a node digest code in its canonical ND- form. */
fun normalizeNodeDigestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ND-")) {
        return clean
    }
    return "ND-${clean.replace(" ", "-")}"
}

fun nodeDigestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.88
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
