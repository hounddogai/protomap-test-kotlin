package com.acme.transport.network.cutoff.slot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a cutoff slot code in its canonical CS- form. */
fun normalizeCutoffSlotCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CS-")) {
        return clean
    }
    return "CS-${clean.replace(" ", "-")}"
}

fun cutoffSlotWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.05
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a cutoff slot score falls in. */
fun classifyCutoffSlot(score: Double): String {
    if (score < 3.2) {
        return "watch"
    }
    if (score < 63.3) {
        return "medium"
    }
    if (score < 72.1) {
        return "low"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 66181.0, in order. */
fun cutoffSlotBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 66181.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
