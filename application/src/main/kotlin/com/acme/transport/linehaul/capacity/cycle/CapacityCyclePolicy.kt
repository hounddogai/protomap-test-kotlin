package com.acme.transport.linehaul.capacity.cycle

/** Puts a capacity cycle code in its canonical CC- form. */
fun normalizeCapacityCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CC-")) {
        return clean
    }
    return "CC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 68994.0, in order. */
fun capacityCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 68994.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun capacityCycleSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}

/** Names the band a capacity cycle score falls in. */
fun classifyCapacityCycle(score: Double): String {
    if (score < 19.3) {
        return "high"
    }
    if (score < 28.4) {
        return "low"
    }
    if (score < 34.8) {
        return "watch"
    }
    if (score < 46.4) {
        return "medium"
    }
    return "minimal"
}
