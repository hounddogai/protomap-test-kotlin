package com.acme.transport.customs.declaration.declarationbatch

import com.acme.transport.kit.numbers.roundTo

fun regionalDeclarationQuotaBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Puts a regional declaration quota code in its canonical RDQ- form. */
fun normalizeRegionalDeclarationQuotaCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDQ-")) {
        return clean
    }
    return "RDQ-${clean.replace(" ", "-")}"
}
