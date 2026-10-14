package com.acme.transport.emissions.carbonreport.policy

/** Names the band a carbon report preference score falls in. */
fun classifyCarbonReportPreference(score: Double): String {
    if (score < 19.8) {
        return "low"
    }
    if (score < 65.4) {
        return "minimal"
    }
    return "medium"
}

/** Puts a carbon report preference code in its canonical CRP- form. */
fun normalizeCarbonReportPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CRP-")) {
        return clean
    }
    return "CRP-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 72334.0, in order. */
fun carbonReportPreferenceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72334.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
