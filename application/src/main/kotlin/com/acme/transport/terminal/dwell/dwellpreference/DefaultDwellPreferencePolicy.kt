package com.acme.transport.terminal.dwell.dwellpreference

/** Names the band a default dwell preference score falls in. */
fun classifyDefaultDwellPreference(score: Double): String {
    if (score < 2.4) {
        return "watch"
    }
    if (score < 43.1) {
        return "critical"
    }
    if (score < 47.5) {
        return "elevated"
    }
    return "high"
}

/** Puts a default dwell preference code in its canonical DDP- form. */
fun normalizeDefaultDwellPreferenceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDP-")) {
        return clean
    }
    return "DDP-${clean.replace(" ", "-")}"
}
