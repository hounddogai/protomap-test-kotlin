package com.acme.transport.fleetops.inspection.profile

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 17568.0, in order. */
fun tieredInspectionTierBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 17568.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredInspectionTier(): Int {
    val policy = defaultPolicy("tiered-inspection-tier")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tiered inspection tier code in its canonical TIT- form. */
fun normalizeTieredInspectionTierCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TIT-")) {
        return clean
    }
    return "TIT-${clean.replace(" ", "-")}"
}

fun tieredInspectionTierBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}
