package com.acme.transport.terminal.traileryard.review

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun strictTrailerYardTemplateWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.04
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun strictTrailerYardTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

/** Counts how many amounts fit in a budget of 86239.0, in order. */
fun strictTrailerYardTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 86239.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a strict trailer yard template code in its canonical STYT- form. */
fun normalizeStrictTrailerYardTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("STYT-")) {
        return clean
    }
    return "STYT-${clean.replace(" ", "-")}"
}
