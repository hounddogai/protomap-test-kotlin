package com.acme.transport.emissions.carbonreport.reportbudget

import com.acme.transport.kit.retry.defaultPolicy

/** Runs the default retry policy against a probe that succeeds late. */
fun probeFallbackCarbonReportBudget(): Int {
    val policy = defaultPolicy("fallback-carbon-report-budget")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 4 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

/** Puts a fallback carbon report budget code in its canonical FCRB- form. */
fun normalizeFallbackCarbonReportBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCRB-")) {
        return clean
    }
    return "FCRB-${clean.replace(" ", "-")}"
}
