package com.acme.transport.linehaul.laneplan.budget

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.retry.defaultPolicy
import kotlin.math.round

/** Counts how many amounts fit in a budget of 16747.0, in order. */
fun lenientLanePlanPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16747.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeLenientLanePlanPreference(): Int {
    val policy = defaultPolicy("lenient-lane-plan-preference")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a lenient lane plan preference code in its canonical LLPP- form. */
fun normalizeLenientLanePlanPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LLPP-")) {
        return clean
    }
    return "LLPP-${clean.replace(" ", "-")}"
}

fun lenientLanePlanPreferenceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.11
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
