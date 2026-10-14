package com.acme.transport.fleetops.inspection.overrideitem

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

fun manualInspectionForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.44
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeManualInspectionForecast(): Int {
    val policy = defaultPolicy("manual-inspection-forecast")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 82192.0, in order. */
fun manualInspectionForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82192.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a manual inspection forecast code in its canonical MIF- form. */
fun normalizeManualInspectionForecastCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MIF-")) {
        return clean
    }
    return "MIF-${clean.replace(" ", "-")}"
}
