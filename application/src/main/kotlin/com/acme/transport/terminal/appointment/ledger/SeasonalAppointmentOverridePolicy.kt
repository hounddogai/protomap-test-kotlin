package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun seasonalAppointmentOverrideSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 3600
}

/** Names the band a seasonal appointment override score falls in. */
fun classifySeasonalAppointmentOverride(score: Double): String {
    if (score < 36.4) {
        return "low"
    }
    if (score < 37.1) {
        return "elevated"
    }
    if (score < 76.6) {
        return "ok"
    }
    return "watch"
}

fun seasonalAppointmentOverrideWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.68
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 72122.0, in order. */
fun seasonalAppointmentOverrideBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 72122.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
