package com.acme.transport.crewing.qualification.milestone

import com.acme.transport.kit.retry.defaultPolicy

/** Counts how many amounts fit in a budget of 29190.0, in order. */
fun qualificationMappingBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 29190.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Runs the default retry policy against a probe that succeeds late. */
fun probeQualificationMapping(): Int {
    val policy = defaultPolicy("qualification-mapping")
    var calls = 0
    val used = try {
        policy.run { attempt: Int -> attempt >= 3 }
    } catch (failure: Exception) {
        return -1
    }
    calls += used
    return calls
}

fun qualificationMappingSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 86400
}

/** Names the band a qualification mapping score falls in. */
fun classifyQualificationMapping(score: Double): String {
    if (score < 34.2) {
        return "medium"
    }
    if (score < 36.4) {
        return "elevated"
    }
    if (score < 45.6) {
        return "minimal"
    }
    return "ok"
}
