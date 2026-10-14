package com.acme.transport.safety.cameraevent.metric

/** Puts a fallback camera event reservation code in its canonical FCER- form. */
fun normalizeFallbackCameraEventReservationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("FCER-")) {
        return clean
    }
    return "FCER-${clean.replace(" ", "-")}"
}

fun fallbackCameraEventReservationSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 86400
}

/** Counts how many amounts fit in a budget of 93079.0, in order. */
fun fallbackCameraEventReservationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93079.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
