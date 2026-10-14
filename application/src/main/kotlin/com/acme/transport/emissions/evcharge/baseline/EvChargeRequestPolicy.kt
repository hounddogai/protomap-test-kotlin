package com.acme.transport.emissions.evcharge.baseline

/** Puts a ev charge request code in its canonical ECR- form. */
fun normalizeEvChargeRequestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ECR-")) {
        return clean
    }
    return "ECR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 71797.0, in order. */
fun evChargeRequestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 71797.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a ev charge request score falls in. */
fun classifyEvChargeRequest(score: Double): String {
    if (score < 11.9) {
        return "critical"
    }
    if (score < 97.2) {
        return "high"
    }
    return "ok"
}
