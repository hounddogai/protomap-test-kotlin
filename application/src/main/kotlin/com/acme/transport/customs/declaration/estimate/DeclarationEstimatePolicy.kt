package com.acme.transport.customs.declaration.estimate

import com.acme.transport.kit.numbers.roundTo

fun declarationEstimateBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a declaration estimate code in its canonical DE- form. */
fun normalizeDeclarationEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DE-")) {
        return clean
    }
    return "DE-${clean.replace(" ", "-")}"
}
