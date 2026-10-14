package com.acme.transport.lastmile.proofofdelivery.deliveryscore

/** Puts a seasonal proof of delivery milestone code in its canonical SPODM- form. */
fun normalizeSeasonalProofOfDeliveryMilestoneCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPODM-")) {
        return clean
    }
    return "SPODM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 997.0, in order. */
fun seasonalProofOfDeliveryMilestoneBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 997.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
