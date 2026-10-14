package com.acme.transport.safety.coaching.coachingmetric

/** Names the band a pending coaching budget score falls in. */
fun classifyPendingCoachingBudget(score: Double): String {
    if (score < 21.4) {
        return "medium"
    }
    if (score < 69.6) {
        return "ok"
    }
    return "high"
}

/** Puts a pending coaching budget code in its canonical PCB- form. */
fun normalizePendingCoachingBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCB-")) {
        return clean
    }
    return "PCB-${clean.replace(" ", "-")}"
}
