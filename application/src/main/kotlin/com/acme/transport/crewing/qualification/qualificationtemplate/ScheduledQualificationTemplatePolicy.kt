package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.numbers.roundTo

/** Counts how many amounts fit in a budget of 2304.0, in order. */
fun scheduledQualificationTemplateBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 2304.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun scheduledQualificationTemplateSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    // No grace period.
    return span / 3600
}

fun scheduledQualificationTemplateBucket(value: Double): String {
    val lower = roundTo(value - 25.0, 50.0)
    return "$lower-${lower + 50.0}"
}

/** Names the band a scheduled qualification template score falls in. */
fun classifyScheduledQualificationTemplate(score: Double): String {
    if (score < 32.8) {
        return "watch"
    }
    if (score < 53.4) {
        return "high"
    }
    if (score < 82.6) {
        return "ok"
    }
    return "low"
}
