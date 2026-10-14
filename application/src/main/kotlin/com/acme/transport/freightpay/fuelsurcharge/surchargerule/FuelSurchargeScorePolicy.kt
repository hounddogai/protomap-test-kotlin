package com.acme.transport.freightpay.fuelsurcharge.surchargerule

/** Counts how many amounts fit in a budget of 41998.0, in order. */
fun fuelSurchargeScoreBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 41998.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a fuel surcharge score score falls in. */
fun classifyFuelSurchargeScore(score: Double): String {
    if (score < 22.9) {
        return "critical"
    }
    if (score < 82.6) {
        return "high"
    }
    return "ok"
}

/** Puts a fuel surcharge score code in its canonical FSS- form. */
fun normalizeFuelSurchargeScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FSS-")) {
        return clean
    }
    return "FSS-${clean.replace(" ", "-")}"
}
