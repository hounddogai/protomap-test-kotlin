package com.acme.transport.terminal.appointment.allocation

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 12739.0, in order. */
fun appointmentBaselineBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 12739.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun appointmentBaselineWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.02
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a appointment baseline code in its canonical AB- form. */
fun normalizeAppointmentBaselineCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AB-")) {
        return clean
    }
    return "AB-${clean.replace(" ", "-")}"
}
