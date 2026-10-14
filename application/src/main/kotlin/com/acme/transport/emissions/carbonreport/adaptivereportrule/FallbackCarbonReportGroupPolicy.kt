package com.acme.transport.emissions.carbonreport.adaptivereportrule

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 8796.0, in order. */
fun fallbackCarbonReportGroupBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 8796.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a fallback carbon report group code in its canonical FCRG- form. */
fun normalizeFallbackCarbonReportGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCRG-")) {
        return clean
    }
    return "FCRG-${clean.replace(" ", "-")}"
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackCarbonReportGroup(): Int {
    val policy = defaultPolicy("fallback-carbon-report-group")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 1 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}
