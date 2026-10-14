package com.acme.transport.emissions.emissionfactor.profile

import com.acme.transport.kit.numbers.roundTo

fun tieredEmissionFactorBatchBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a tiered emission factor batch code in its canonical TEFB- form. */
fun normalizeTieredEmissionFactorBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TEFB-")) {
        return clean
    }
    return "TEFB-${clean.replace(" ", "-")}"
}
