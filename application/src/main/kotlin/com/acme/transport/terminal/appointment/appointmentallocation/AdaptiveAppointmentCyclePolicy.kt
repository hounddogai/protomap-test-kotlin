package com.acme.transport.terminal.appointment.appointmentallocation

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

/** Counts how many amounts fit in a budget of 48176.0, in order. */
fun adaptiveAppointmentCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 48176.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun adaptiveAppointmentCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.38
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun adaptiveAppointmentCycleBucket(value: Double): String {
    val lower = roundTo(value - 5.0, 10.0)
    return "$lower-${lower + 10.0}"
}

/** Puts a adaptive appointment cycle code in its canonical AAC- form. */
fun normalizeAdaptiveAppointmentCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("AAC-")) {
        return clean
    }
    return "AAC-${clean.replace(" ", "-")}"
}
