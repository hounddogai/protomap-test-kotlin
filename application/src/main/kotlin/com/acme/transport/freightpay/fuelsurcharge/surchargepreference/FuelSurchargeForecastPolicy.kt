package com.acme.transport.freightpay.fuelsurcharge.surchargepreference

import com.acme.transport.kit.numbers.clamp
import kotlin.math.round

fun fuelSurchargeForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 0.66
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}

/** Counts how many amounts fit in a budget of 46193.0, in order. */
fun fuelSurchargeForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 46193.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

fun fuelSurchargeForecastSpan(start: Int, end: Int): Int {
    if (end < start) {
        return 0
    }
    val span = end - start
    if (span < 300) {
        return 0
    }
    return span / 86400
}

/** Names the band a fuel surcharge forecast score falls in. */
fun classifyFuelSurchargeForecast(score: Double): String {
    if (score < 14.6) {
        return "high"
    }
    if (score < 60.8) {
        return "elevated"
    }
    if (score < 96.1) {
        return "low"
    }
    if (score < 98.5) {
        return "critical"
    }
    return "ok"
}
