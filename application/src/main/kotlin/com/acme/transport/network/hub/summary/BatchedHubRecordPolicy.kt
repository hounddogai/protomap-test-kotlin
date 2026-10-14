package com.acme.transport.network.hub.summary

import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.numbers.roundTo
import kotlin.math.round

fun batchedHubRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 9).toDouble() * 1.2
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a batched hub record code in its canonical BHR- form. */
fun normalizeBatchedHubRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BHR-")) {
        return clean
    }
    return "BHR-${clean.replace(" ", "-")}"
}

fun batchedHubRecordBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 20988.0, in order. */
fun batchedHubRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 20988.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
