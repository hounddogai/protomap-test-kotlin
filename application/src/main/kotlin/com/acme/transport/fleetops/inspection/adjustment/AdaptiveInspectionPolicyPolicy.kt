package com.acme.transport.fleetops.inspection.adjustment

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveInspectionPolicy(): Int {
    val policy = defaultPolicy("adaptive-inspection-policy")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive inspection policy code in its canonical AIP- form. */
fun normalizeAdaptiveInspectionPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AIP-")) {
        return clean
    }
    return "AIP-${clean.replace(" ", "-")}"
}

fun adaptiveInspectionPolicyWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.87
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
