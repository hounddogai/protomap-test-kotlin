package com.acme.transport.customs.tariff.manifest

/** Counts how many amounts fit in a budget of 9152.0, in order. */
fun tariffManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 9152.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tariff manifest code in its canonical TM- form. */
fun normalizeTariffManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TM-")) {
        return clean
    }
    return "TM-${clean.replace(" ", "-")}"
}
