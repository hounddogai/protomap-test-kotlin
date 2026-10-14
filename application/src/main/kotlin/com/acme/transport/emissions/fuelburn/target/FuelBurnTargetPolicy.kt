package com.acme.transport.emissions.fuelburn.target

import com.acme.transport.kit.numbers.roundTo
import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFuelBurnTarget(): Int {
    val policy = defaultPolicy("fuel-burn-target")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 789.0, in order. */
fun fuelBurnTargetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 789.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelBurnTargetBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a fuel burn target code in its canonical FBT- form. */
fun normalizeFuelBurnTargetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FBT-")) {
        return clean
    }
    return "FBT-${clean.replace(" ", "-")}"
}
