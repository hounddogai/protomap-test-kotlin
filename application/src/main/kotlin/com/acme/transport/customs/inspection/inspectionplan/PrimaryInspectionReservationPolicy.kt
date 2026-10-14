package com.acme.transport.customs.inspection.inspectionplan

fun primaryInspectionReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 43235.0, in order. */
fun primaryInspectionReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 43235.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a primary inspection reservation code in its canonical PIR- form. */
fun normalizePrimaryInspectionReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PIR-")) {
        return clean
    }
    return "PIR-${clean.replace(" ", "-")}"
}

/** Names the band a primary inspection reservation score falls in. */
fun classifyPrimaryInspectionReservation(score: Double): String {
    if (score < 35.1) {
        return "watch"
    }
    if (score < 65.1) {
        return "critical"
    }
    if (score < 72.7) {
        return "ok"
    }
    if (score < 74.6) {
        return "low"
    }
    return "elevated"
}
