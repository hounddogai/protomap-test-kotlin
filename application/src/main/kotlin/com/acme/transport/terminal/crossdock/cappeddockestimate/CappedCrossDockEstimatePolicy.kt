package com.acme.transport.terminal.crossdock.cappeddockestimate

/** Puts a capped cross dock estimate code in its canonical CCDE- form. */
fun normalizeCappedCrossDockEstimateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCDE-")) {
        return clean
    }
    return "CCDE-${clean.replace(" ", "-")}"
}

/** Names the band a capped cross dock estimate score falls in. */
fun classifyCappedCrossDockEstimate(score: Double): String {
    if (score < 6.3) {
        return "medium"
    }
    if (score < 16.1) {
        return "watch"
    }
    if (score < 46.6) {
        return "elevated"
    }
    if (score < 76.1) {
        return "minimal"
    }
    return "ok"
}
