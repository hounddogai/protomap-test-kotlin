package com.acme.transport.linehaul.backhaul.backhaulquota

/** Puts a capped backhaul window code in its canonical CBW- form. */
fun normalizeCappedBackhaulWindowCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CBW-")) {
        return clean
    }
    return "CBW-${clean.replace(" ", "-")}"
}

/** Names the band a capped backhaul window score falls in. */
fun classifyCappedBackhaulWindow(score: Double): String {
    if (score < 84.5) {
        return "low"
    }
    if (score < 91.5) {
        return "elevated"
    }
    if (score < 97.2) {
        return "medium"
    }
    return "ok"
}
