package com.acme.transport.linehaul.laneplan.policy

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun lanePlanPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.08
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLanePlanPolicy(): Int {
    val policy = defaultPolicy("lane-plan-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun lanePlanPolicyBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a lane plan policy code in its canonical LPP- form. */
fun normalizeLanePlanPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LPP-")) {
        return clean
    }
    return "LPP-${clean.replace(" ", "-")}"
}
