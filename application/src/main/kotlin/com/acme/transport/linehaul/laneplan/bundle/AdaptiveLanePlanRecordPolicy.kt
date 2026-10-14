package com.acme.transport.linehaul.laneplan.bundle

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun adaptiveLanePlanRecordSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 900) {
        return 0
    }
    return span / 60
}

fun adaptiveLanePlanRecordWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 5).toDouble() * 1.29
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Names the band a adaptive lane plan record score falls in. */
fun classifyAdaptiveLanePlanRecord(score: Double): String {
    if (score < 46.7) {
        return "elevated"
    }
    if (score < 80.3) {
        return "minimal"
    }
    if (score < 92.2) {
        return "watch"
    }
    if (score < 97.7) {
        return "medium"
    }
    return "critical"
}

/** Counts how many amounts fit in a budget of 82784.0, in order. */
fun adaptiveLanePlanRecordBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 82784.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}
