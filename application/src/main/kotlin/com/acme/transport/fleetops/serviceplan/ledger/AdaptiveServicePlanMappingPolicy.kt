package com.acme.transport.fleetops.serviceplan.ledger

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

fun adaptiveServicePlanMappingBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 88319.0, in order. */
fun adaptiveServicePlanMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 88319.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveServicePlanMapping(): Int {
    val policy = defaultPolicy("adaptive-service-plan-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a adaptive service plan mapping code in its canonical ASPM- form. */
fun normalizeAdaptiveServicePlanMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ASPM-")) {
        return clean
    }
    return "ASPM-${clean.replace(" ", "-")}"
}
