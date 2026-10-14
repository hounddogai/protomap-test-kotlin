package com.acme.transport.terminal.dockdoor.score

/** Names the band a dock door score score falls in. */
fun classifyDockDoorScore(score: Double): String {
    if (score < 11.3) {
        return "ok"
    }
    if (score < 68.3) {
        return "high"
    }
    return "low"
}

/** Puts a dock door score code in its canonical DDS- form. */
fun normalizeDockDoorScoreCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDS-")) {
        return clean
    }
    return "DDS-${clean.replace(" ", "-")}"
}
