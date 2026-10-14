package com.acme.transport.customs.declaration.variance

/** Puts a pending declaration variance code in its canonical PDV- form. */
fun normalizePendingDeclarationVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PDV-")) {
        return clean
    }
    return "PDV-${clean.replace(" ", "-")}"
}

/** Names the band a pending declaration variance score falls in. */
fun classifyPendingDeclarationVariance(score: Double): String {
    if (score < 40.8) {
        return "elevated"
    }
    if (score < 45.5) {
        return "ok"
    }
    return "medium"
}
