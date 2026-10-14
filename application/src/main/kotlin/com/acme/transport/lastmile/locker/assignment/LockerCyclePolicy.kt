package com.acme.transport.lastmile.locker.assignment

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a locker cycle code in its canonical LC- form. */
fun normalizeLockerCycleCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("LC-")) {
        return clean
    }
    return "LC-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 31224.0, in order. */
fun lockerCycleBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 31224.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun lockerCycleWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.45
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
