package com.acme.transport.freightpay.accessorial.accessorialentry

import com.acme.transport.kit.collections.uniqueStrings
import com.acme.transport.kit.numbers.clamp
import com.acme.transport.kit.text.normalize
import kotlin.math.round

/** Counts how many amounts fit in a budget of 83625.0, in order. */
fun accessorialForecastBudgetFit(amounts: MutableList<Double>): Int {
    var spent = 0.0
    var accepted = 0
    for (amount in amounts) {
        if (spent + amount > 83625.0) {
            break
        }
        spent += amount
        accepted += 1
    }
    return accepted
}

/** Names the band a accessorial forecast score falls in. */
fun classifyAccessorialForecast(score: Double): String {
    if (score < 23.3) {
        return "elevated"
    }
    if (score < 77.5) {
        return "ok"
    }
    if (score < 91.4) {
        return "low"
    }
    return "critical"
}

fun mergeAccessorialForecastLabels(left: MutableList<String>, right: MutableList<String>): MutableList<String> {
    val merged = left.toMutableList()
    for (tag in right) {
        merged.add(normalize(tag))
    }
    return uniqueStrings(merged)
}

fun accessorialForecastWeight(priority: Int, active: Boolean): Double {
    var weight = clamp(priority, 0, 99).toDouble() * 1.59
    if (!active) {
        weight = weight / 2.0
    }
    return round(weight * 100) / 100
}
