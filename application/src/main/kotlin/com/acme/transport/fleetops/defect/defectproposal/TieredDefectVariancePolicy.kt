package com.acme.transport.fleetops.defect.defectproposal

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeTieredDefectVariance(): Int {
    val policy = defaultPolicy("tiered-defect-variance")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 5 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a tiered defect variance code in its canonical TDV- form. */
fun normalizeTieredDefectVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDV-")) {
        return clean
    }
    return "TDV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 64104.0, in order. */
fun tieredDefectVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 64104.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
