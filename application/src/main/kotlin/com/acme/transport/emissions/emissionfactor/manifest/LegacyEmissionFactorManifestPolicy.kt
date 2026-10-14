package com.acme.transport.emissions.emissionfactor.manifest

/** Counts how many amounts fit in a budget of 310.0, in order. */
fun legacyEmissionFactorManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 310.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a legacy emission factor manifest code in its canonical LEFM- form. */
fun normalizeLegacyEmissionFactorManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LEFM-")) {
        return clean
    }
    return "LEFM-${clean.replace(" ", "-")}"
}
