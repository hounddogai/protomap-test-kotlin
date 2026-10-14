package com.acme.transport.customs.hold.audit

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeStrictHoldBundle(): Int {
    val policy = defaultPolicy("strict-hold-bundle")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a strict hold bundle code in its canonical SHB- form. */
fun normalizeStrictHoldBundleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SHB-")) {
        return clean
    }
    return "SHB-${clean.replace(" ", "-")}"
}

fun strictHoldBundleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.12
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictHoldBundleBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
