package com.acme.transport.customs.hold.request

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun manualHoldManifestWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a manual hold manifest code in its canonical MHM- form. */
fun normalizeManualHoldManifestCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("MHM-")) {
        return clean
    }
    return "MHM-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 99196.0, in order. */
fun manualHoldManifestBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 99196.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun manualHoldManifestSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
