package com.acme.transport.fleetops.defect.defectcheckpoint

import com.acme.transport.kit.retry.defaultPolicy

/** Puts a adaptive defect plan code in its canonical ADP- form. */
fun normalizeAdaptiveDefectPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADP-")) {
        return clean
    }
    return "ADP-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeAdaptiveDefectPlan(): Int {
    val policy = defaultPolicy("adaptive-defect-plan")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 2 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Counts how many amounts fit in a budget of 80768.0, in order. */
fun adaptiveDefectPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 80768.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
