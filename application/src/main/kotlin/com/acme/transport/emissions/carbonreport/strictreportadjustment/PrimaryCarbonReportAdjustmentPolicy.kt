package com.acme.transport.emissions.carbonreport.strictreportadjustment

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 92495.0, in order. */
fun primaryCarbonReportAdjustmentBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 92495.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary carbon report adjustment code in its canonical PCRA- form. */
fun normalizePrimaryCarbonReportAdjustmentCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCRA-")) {
        return clean
    }
    return "PCRA-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probePrimaryCarbonReportAdjustment(): Int {
    val policy = defaultPolicy("primary-carbon-report-adjustment")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
