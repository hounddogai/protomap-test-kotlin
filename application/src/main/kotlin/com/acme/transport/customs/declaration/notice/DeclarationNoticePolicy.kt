package com.acme.transport.customs.declaration.notice

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun declarationNoticeWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.1
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 1875.0, in order. */
fun declarationNoticeBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 1875.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a declaration notice score falls in. */
fun classifyDeclarationNotice(score: Double): String {
    if (score < 22.0) {
        return "ok"
    }
    if (score < 33.2) {
        return "medium"
    }
    if (score < 72.4) {
        return "high"
    }
    if (score < 96.7) {
        return "watch"
    }
    return "minimal"
}
