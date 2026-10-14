package com.acme.transport.emissions.emissionfactor.factortarget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 34347.0, in order. */
fun adaptiveEmissionFactorMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34347.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a adaptive emission factor milestone code in its canonical AEFM- form. */
fun normalizeAdaptiveEmissionFactorMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AEFM-")) {
        return clean
    }
    return "AEFM-${clean.replace(" ", "-")}"
}

fun adaptiveEmissionFactorMilestoneWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.51
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveEmissionFactorMilestone(): Int {
    val policy = defaultPolicy("adaptive-emission-factor-milestone")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
