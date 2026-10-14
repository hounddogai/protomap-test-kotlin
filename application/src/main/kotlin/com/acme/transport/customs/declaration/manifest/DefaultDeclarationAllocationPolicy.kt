package com.acme.transport.customs.declaration.manifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun defaultDeclarationAllocationWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.56
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a default declaration allocation code in its canonical DDA- form. */
fun normalizeDefaultDeclarationAllocationCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("DDA-")) {
        return clean
    }
    return "DDA-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 44525.0, in order. */
fun defaultDeclarationAllocationBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 44525.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
