package com.acme.transport.linehaul.capacity.capacitymilestone

/** Names the band a capped capacity policy score falls in. */
fun classifyCappedCapacityPolicy(score: Double): String {
    if (score < 7.0) {
        return "minimal"
    }
    if (score < 67.3) {
        return "low"
    }
    return "watch"
}

/** Puts a capped capacity policy code in its canonical CCP- form. */
fun normalizeCappedCapacityPolicyCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CCP-")) {
        return clean
    }
    return "CCP-${clean.replace(" ", "-")}"
}
