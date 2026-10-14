package com.acme.transport.customs.declaration.forecast

import com.acme.transport.kit.numbers.roundTo

fun defaultDeclarationMappingBucket(value: Double): String {
    val lower = roundTo(value - 50.0, 100.0)
    return "$lower-${lower + 100.0}"
}

/** Puts a default declaration mapping code in its canonical DDM- form. */
fun normalizeDefaultDeclarationMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDM-")) {
        return clean
    }
    return "DDM-${clean.replace(" ", "-")}"
}

/** Names the band a default declaration mapping score falls in. */
fun classifyDefaultDeclarationMapping(score: Double): String {
    if (score < 71.8) {
        return "elevated"
    }
    if (score < 80.1) {
        return "ok"
    }
    return "minimal"
}
