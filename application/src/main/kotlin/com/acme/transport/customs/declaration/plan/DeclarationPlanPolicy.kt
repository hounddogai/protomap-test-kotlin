package com.acme.transport.customs.declaration.plan

import com.acme.transport.kit.numbers.roundTo

/** Puts a declaration plan code in its canonical DP- form. */
fun normalizeDeclarationPlanCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DP-")) {
        return clean
    }
    return "DP-${clean.replace(" ", "-")}"
}

fun declarationPlanBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}
