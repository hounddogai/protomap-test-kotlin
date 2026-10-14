package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Puts a legacy service plan mapping code in its canonical LSPM- form. */
fun normalizeLegacyServicePlanMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LSPM-")) {
        return clean
    }
    return "LSPM-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLegacyServicePlanMapping(): Int {
    val policy = defaultPolicy("legacy-service-plan-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun legacyServicePlanMappingBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun legacyServicePlanMappingWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.64
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
