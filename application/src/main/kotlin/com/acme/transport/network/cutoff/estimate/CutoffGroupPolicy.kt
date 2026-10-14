package com.acme.transport.network.cutoff.estimate

/** Names the band a cutoff group score falls in. */
fun classifyCutoffGroup(score: Double): String {
    if (score < 16.5) {
        return "elevated"
    }
    if (score < 48.5) {
        return "ok"
    }
    if (score < 50.2) {
        return "medium"
    }
    return "minimal"
}

/** Puts a cutoff group code in its canonical CG- form. */
fun normalizeCutoffGroupCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CG-")) {
        return clean
    }
    return "CG-${clean.replace(" ", "-")}"
}
