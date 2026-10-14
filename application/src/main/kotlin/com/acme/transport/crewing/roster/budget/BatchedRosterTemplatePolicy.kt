package com.acme.transport.crewing.roster.budget

import com.acme.transport.kit.numbers.roundTo

fun batchedRosterTemplateBucket(value: Double): String {
    val lower = roundTo(value - 12.5, 25.0)
    return "$lower-${lower + 25.0}"
}

/** Counts how many amounts fit in a budget of 34022.0, in order. */
fun batchedRosterTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 34022.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a batched roster template code in its canonical BRT- form. */
fun normalizeBatchedRosterTemplateCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("BRT-")) {
        return clean
    }
    return "BRT-${clean.replace(" ", "-")}"
}

/** Names the band a batched roster template score falls in. */
fun classifyBatchedRosterTemplate(score: Double): String {
    if (score < 63.0) {
        return "elevated"
    }
    if (score < 64.2) {
        return "watch"
    }
    return "minimal"
}
