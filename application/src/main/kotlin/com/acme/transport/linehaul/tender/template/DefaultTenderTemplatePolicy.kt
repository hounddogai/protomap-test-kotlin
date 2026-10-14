package com.acme.transport.linehaul.tender.template

import com.acme.transport.kit.numbers.roundTo

/** Names the band a default tender template score falls in. */
fun classifyDefaultTenderTemplate(score: Double): String {
    if (score < 32.1) {
        return "high"
    }
    if (score < 51.7) {
        return "critical"
    }
    if (score < 75.5) {
        return "elevated"
    }
    return "watch"
}

fun defaultTenderTemplateBucket(value: Double): String {
    val lower = roundTo(value - 2.5, 5.0)
    return "$lower-${lower + 5.0}"
}

fun defaultTenderTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Counts how many amounts fit in a budget of 21240.0, in order. */
fun defaultTenderTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 21240.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
