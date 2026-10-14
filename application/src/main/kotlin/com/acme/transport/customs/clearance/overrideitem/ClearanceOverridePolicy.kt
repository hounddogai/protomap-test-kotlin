package com.acme.transport.customs.clearance.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun clearanceOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.8
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeClearanceOverride(): Int {
    val policy = defaultPolicy("clearance-override")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a clearance override code in its canonical CO- form. */
fun normalizeClearanceOverrideCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CO-")) {
        return clean
    }
    return "CO-${clean.replace(" ", "-")}"
}
