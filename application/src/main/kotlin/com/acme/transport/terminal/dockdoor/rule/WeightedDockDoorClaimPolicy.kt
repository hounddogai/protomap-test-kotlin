package com.acme.transport.terminal.dockdoor.rule

/** Names the band a weighted dock door claim score falls in. */
fun classifyWeightedDockDoorClaim(score: Double): String {
    if (score < 22.2) {
        return "low"
    }
    if (score < 67.6) {
        return "elevated"
    }
    return "ok"
}

/** Puts a weighted dock door claim code in its canonical WDDC- form. */
fun normalizeWeightedDockDoorClaimCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("WDDC-")) {
        return clean
    }
    return "WDDC-${clean.replace(" ", "-")}"
}

fun weightedDockDoorClaimSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
