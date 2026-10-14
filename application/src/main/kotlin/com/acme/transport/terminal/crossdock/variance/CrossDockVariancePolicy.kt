package com.acme.transport.terminal.crossdock.variance

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

fun crossDockVarianceWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 2.99
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a cross dock variance code in its canonical CDV- form. */
fun normalizeCrossDockVarianceCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CDV-")) {
        return clean
    }
    return "CDV-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 22634.0, in order. */
fun crossDockVarianceBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 22634.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun mergeCrossDockVarianceLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}
