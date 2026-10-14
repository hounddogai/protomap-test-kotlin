package com.acme.transport.crewing.qualification.plan

/** Counts how many amounts fit in a budget of 98438.0, in order. */
fun qualificationPlanBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 98438.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a qualification plan score falls in. */
fun classifyQualificationPlan(score: Double): String {
    if (score < 34.6) {
        return "low"
    }
    if (score < 76.5) {
        return "watch"
    }
    if (score < 84.5) {
        return "elevated"
    }
    if (score < 89.6) {
        return "medium"
    }
    return "ok"
}

fun qualificationPlanSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 60) {
        return 0
    }
    return span / 3600
}
