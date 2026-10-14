package com.acme.transport.fleetops.tire.tireagreement

/** Counts how many amounts fit in a budget of 99804.0, in order. */
fun tieredTireBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99804.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered tire batch code in its canonical TTB- form. */
fun normalizeTieredTireBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TTB-")) {
        return clean
    }
    return "TTB-${clean.replace(" ", "-")}"
}
