package com.acme.transport.lastmile.drop.forecast

/** Puts a tiered drop template code in its canonical TDT- form. */
fun normalizeTieredDropTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDT-")) {
        return clean
    }
    return "TDT-${clean.replace(" ", "-")}"
}

/** Names the band a tiered drop template score falls in. */
fun classifyTieredDropTemplate(score: Double): String {
    if (score < 27.4) {
        return "ok"
    }
    if (score < 93.6) {
        return "watch"
    }
    if (score < 94.2) {
        return "high"
    }
    return "critical"
}
