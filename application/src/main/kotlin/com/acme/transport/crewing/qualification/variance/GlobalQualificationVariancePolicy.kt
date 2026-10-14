package com.acme.transport.crewing.qualification.variance

/** Names the band a global qualification variance score falls in. */
fun classifyGlobalQualificationVariance(score: Double): String {
    if (score < 39.9) {
        return "ok"
    }
    if (score < 87.2) {
        return "high"
    }
    return "elevated"
}

/** Puts a global qualification variance code in its canonical GQV- form. */
fun normalizeGlobalQualificationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GQV-")) {
        return clean
    }
    return "GQV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 72728.0, in order. */
fun globalQualificationVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72728.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
