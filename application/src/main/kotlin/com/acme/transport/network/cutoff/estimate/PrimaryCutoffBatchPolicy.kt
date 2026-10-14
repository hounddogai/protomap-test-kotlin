package com.acme.transport.network.cutoff.estimate

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Counts how many amounts fit in a budget of 217.0, in order. */
fun primaryCutoffBatchBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 217.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun primaryCutoffBatchSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 60
}

/** Puts a primary cutoff batch code in its canonical PCB- form. */
fun normalizePrimaryCutoffBatchCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("PCB-")) {
        return clean
    }
    return "PCB-${clean.replace(" ", "-")}"
}

fun primaryCutoffBatchWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 0.55
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
