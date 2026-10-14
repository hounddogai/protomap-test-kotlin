package com.acme.transport.emissions.routeoffset.profile

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun routeOffsetReviewBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Counts how many amounts fit in a budget of 87676.0, in order. */
fun routeOffsetReviewBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 87676.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun routeOffsetReviewWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 10).toDouble() * 2.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a route offset review code in its canonical ROR- form. */
fun normalizeRouteOffsetReviewCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("ROR-")) {
        return clean
    }
    return "ROR-${clean.replace(" ", "-")}"
}
