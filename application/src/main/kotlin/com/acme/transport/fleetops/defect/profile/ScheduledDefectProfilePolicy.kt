package com.acme.transport.fleetops.defect.profile

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 23088.0, in order. */
fun scheduledDefectProfileBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 23088.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a scheduled defect profile code in its canonical SDP- form. */
fun normalizeScheduledDefectProfileCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("SDP-")) {
        return clean
    }
    return "SDP-${clean.replace(" ", "-")}"
}

fun scheduledDefectProfileWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 2.27
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a scheduled defect profile score falls in. */
fun classifyScheduledDefectProfile(score: Double): String {
    if (score < 5.6) {
        return "ok"
    }
    if (score < 43.6) {
        return "watch"
    }
    if (score < 66.9) {
        return "high"
    }
    if (score < 89.1) {
        return "minimal"
    }
    return "medium"
}
