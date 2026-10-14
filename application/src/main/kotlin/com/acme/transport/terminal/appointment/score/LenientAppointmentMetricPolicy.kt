package com.acme.transport.terminal.appointment.score

import com.acme.transport.kit.numbers.roundTo

/** Puts a lenient appointment metric code in its canonical LAM- form. */
fun normalizeLenientAppointmentMetricCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LAM-")) {
        return clean
    }
    return "LAM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 25976.0, in order. */
fun lenientAppointmentMetricBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 25976.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lenientAppointmentMetricBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

fun lenientAppointmentMetricSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 3600) {
        return 0
    }
    return span / 86400
}
