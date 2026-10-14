package com.acme.transport.lastmile.drop.preference

/** Names the band a tiered drop threshold score falls in. */
fun classifyTieredDropThreshold(score: Double): String {
    if (score < 24.3) {
        return "critical"
    }
    if (score < 40.7) {
        return "medium"
    }
    return "high"
}

/** Puts a tiered drop threshold code in its canonical TDT- form. */
fun normalizeTieredDropThresholdCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDT-")) {
        return clean
    }
    return "TDT-${clean.replace(" ", "-")}"
}
