package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a global emission factor proposal code in its canonical GEFP- form. */
fun normalizeGlobalEmissionFactorProposalCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("GEFP-")) {
        return clean
    }
    return "GEFP-${clean.replace(" ", "-")}"
}

fun globalEmissionFactorProposalSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 3600
}

fun globalEmissionFactorProposalWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 0.23
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
