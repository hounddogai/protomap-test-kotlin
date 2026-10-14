package com.acme.transport.customs.declaration.threshold

import com.acme.transport.kit.numbers.roundTo

/** Puts a declaration record code in its canonical DR- form. */
fun normalizeDeclarationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DR-")) {
        return clean
    }
    return "DR-${clean.replace(" ", "-")}"
}

fun declarationRecordBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}
