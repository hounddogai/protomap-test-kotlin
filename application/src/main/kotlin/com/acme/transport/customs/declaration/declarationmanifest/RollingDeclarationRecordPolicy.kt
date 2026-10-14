package com.acme.transport.customs.declaration.declarationmanifest

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Puts a rolling declaration record code in its canonical RDR- form. */
fun normalizeRollingDeclarationRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RDR-")) {
        return clean
    }
    return "RDR-${clean.replace(" ", "-")}"
}

fun rollingDeclarationRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 58389.0, in order. */
fun rollingDeclarationRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 58389.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun rollingDeclarationRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 60
}
