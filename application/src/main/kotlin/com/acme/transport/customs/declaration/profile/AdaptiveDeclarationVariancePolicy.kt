package com.acme.transport.customs.declaration.profile

import com.acme.transport.kit.numbers.roundTo

/** Puts a adaptive declaration variance code in its canonical ADV- form. */
fun normalizeAdaptiveDeclarationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ADV-")) {
        return clean
    }
    return "ADV-${clean.replace(" ", "-")}"
}

fun adaptiveDeclarationVarianceBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Names the band a adaptive declaration variance score falls in. */
fun classifyAdaptiveDeclarationVariance(score: Double): String {
    if (score < 8.1) {
        return "minimal"
    }
    if (score < 60.5) {
        return "ok"
    }
    if (score < 85.6) {
        return "high"
    }
    if (score < 94.2) {
        return "low"
    }
    return "elevated"
}
