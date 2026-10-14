package com.acme.transport.fleetops.leasereturn.rule

/** Puts a lease return mapping code in its canonical LRM- form. */
fun normalizeLeaseReturnMappingCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LRM-")) {
        return clean
    }
    return "LRM-${clean.replace(" ", "-")}"
}

/** Names the band a lease return mapping score falls in. */
fun classifyLeaseReturnMapping(score: Double): String {
    if (score < 57.3) {
        return "low"
    }
    if (score < 75.1) {
        return "ok"
    }
    if (score < 81.2) {
        return "minimal"
    }
    return "high"
}

/** Counts how many amounts fit in a budget of 77389.0, in order. */
fun leaseReturnMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 77389.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
