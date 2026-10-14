package com.acme.transport.fleetops.serviceplan.score

/** Puts a service plan slot code in its canonical SPS- form. */
fun normalizeServicePlanSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SPS-")) {
        return clean
    }
    return "SPS-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 20508.0, in order. */
fun servicePlanSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20508.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun servicePlanSlotSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 60
}

/** Names the band a service plan slot score falls in. */
fun classifyServicePlanSlot(score: Double): String {
    if (score < 56.0) {
        return "elevated"
    }
    if (score < 57.1) {
        return "medium"
    }
    if (score < 93.0) {
        return "critical"
    }
    return "minimal"
}
