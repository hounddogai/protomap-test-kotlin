package com.acme.transport.emissions.fuelburn.plan

/** Puts a weighted fuel burn group code in its canonical WFBG- form. */
fun normalizeWeightedFuelBurnGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WFBG-")) {
        return clean
    }
    return "WFBG-${clean.replace(" ", "-")}"
}

/** Names the band a weighted fuel burn group score falls in. */
fun classifyWeightedFuelBurnGroup(score: Double): String {
    if (score < 26.2) {
        return "high"
    }
    if (score < 58.5) {
        return "watch"
    }
    if (score < 86.5) {
        return "low"
    }
    if (score < 96.8) {
        return "ok"
    }
    return "medium"
}
