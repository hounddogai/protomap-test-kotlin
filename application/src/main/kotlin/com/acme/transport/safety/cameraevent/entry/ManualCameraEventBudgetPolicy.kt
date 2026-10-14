package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualCameraEventBudgetWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.06
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 37521.0, in order. */
fun manualCameraEventBudgetBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 37521.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a manual camera event budget score falls in. */
fun classifyManualCameraEventBudget(score: Double): String {
    if (score < 38.2) {
        return "watch"
    }
    if (score < 76.0) {
        return "ok"
    }
    return "elevated"
}

/** Puts a manual camera event budget code in its canonical MCEB- form. */
fun normalizeManualCameraEventBudgetCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MCEB-")) {
        return clean
    }
    return "MCEB-${clean.replace(" ", "-")}"
}
