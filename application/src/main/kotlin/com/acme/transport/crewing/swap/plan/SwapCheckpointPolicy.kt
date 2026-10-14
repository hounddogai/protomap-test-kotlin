package com.acme.transport.crewing.swap.plan

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

/** Names the band a swap checkpoint score falls in. */
fun classifySwapCheckpoint(score: Double): String {
    if (score < 17.6) {
        return "medium"
    }
    if (score < 39.4) {
        return "ok"
    }
    return "minimal"
}

/** Counts how many amounts fit in a budget of 83614.0, in order. */
fun swapCheckpointBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83614.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun swapCheckpointWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.32
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

fun swapCheckpointSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 60
}
