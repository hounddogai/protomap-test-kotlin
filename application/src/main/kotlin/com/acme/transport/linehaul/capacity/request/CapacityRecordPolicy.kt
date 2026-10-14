package com.acme.transport.linehaul.capacity.request

import com.acme.transport.kit.numbers.roundTo

fun capacityRecordBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Puts a capacity record code in its canonical CR- form. */
fun normalizeCapacityRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CR-")) {
        return clean
    }
    return "CR-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 16893.0, in order. */
fun capacityRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 16893.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a capacity record score falls in. */
fun classifyCapacityRecord(score: Double): String {
    if (score < 14.2) {
        return "high"
    }
    if (score < 23.3) {
        return "ok"
    }
    if (score < 29.6) {
        return "watch"
    }
    return "medium"
}
