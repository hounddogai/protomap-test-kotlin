package com.acme.transport.safety.coaching.coachingsnapshot

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun coachingExceptionWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.4
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 57940.0, in order. */
fun coachingExceptionBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 57940.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Puts a coaching exception code in its canonical CE- form. */
fun normalizeCoachingExceptionCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("CE-")) {
        return clean
    }
    return "CE-${clean.replace(" ", "-")}"
}

/** Names the band a coaching exception score falls in. */
fun classifyCoachingException(score: Double): String {
    if (score < 11.2) {
        return "ok"
    }
    if (score < 26.6) {
        return "watch"
    }
    if (score < 27.8) {
        return "medium"
    }
    if (score < 99.4) {
        return "high"
    }
    return "minimal"
}
