package com.acme.transport.crewing.availability.slot

/** Counts how many amounts fit in a budget of 34042.0, in order. */
fun availabilitySlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34042.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a availability slot code in its canonical AS- form. */
fun normalizeAvailabilitySlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AS-")) {
        return clean
    }
    return "AS-${clean.replace(" ", "-")}"
}

/** Names the band a availability slot score falls in. */
fun classifyAvailabilitySlot(score: Double): String {
    if (score < 2.5) {
        return "high"
    }
    if (score < 20.2) {
        return "minimal"
    }
    return "elevated"
}
