package com.acme.transport.fleetops.defect.defectrecord

import com.acme.transport.kit.numbers.roundTo

fun tieredDefectRecordBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Names the band a tiered defect record score falls in. */
fun classifyTieredDefectRecord(score: Double): String {
    if (score < 15.6) {
        return "elevated"
    }
    if (score < 52.2) {
        return "low"
    }
    if (score < 87.7) {
        return "medium"
    }
    if (score < 88.6) {
        return "minimal"
    }
    return "watch"
}

/** Counts how many amounts fit in a budget of 93074.0, in order. */
fun tieredDefectRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 93074.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a tiered defect record code in its canonical TDR- form. */
fun normalizeTieredDefectRecordCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("TDR-")) {
        return clean
    }
    return "TDR-${clean.replace(" ", "-")}"
}
