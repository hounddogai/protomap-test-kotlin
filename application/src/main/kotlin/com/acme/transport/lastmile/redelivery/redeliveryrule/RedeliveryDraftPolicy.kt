package com.acme.transport.lastmile.redelivery.redeliveryrule

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a redelivery draft score falls in. */
fun classifyRedeliveryDraft(score: Double): String {
    if (score < 19.0) {
        return "ok"
    }
    if (score < 29.7) {
        return "low"
    }
    if (score < 30.0) {
        return "watch"
    }
    if (score < 51.7) {
        return "minimal"
    }
    return "elevated"
}

fun redeliveryDraftWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 0.28
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Puts a redelivery draft code in its canonical RD- form. */
fun normalizeRedeliveryDraftCode(code: String): String {
    val clean = code.trim().uppercase()
    if (clean == "") {
        return ""
    }
    if (clean.startsWith("RD-")) {
        return clean
    }
    return "RD-${clean.replace(" ", "-")}"
}

/** Counts how many amounts fit in a budget of 7361.0, in order. */
fun redeliveryDraftBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 7361.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
