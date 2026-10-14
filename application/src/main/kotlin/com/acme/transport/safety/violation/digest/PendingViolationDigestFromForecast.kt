package com.acme.transport.safety.violation.digest

import com.acme.transport.safety.violation.forecast.StrictViolationForecast
import com.acme.transport.safety.violation.forecast.strictViolationForecastWeight

fun summarizeStrictViolationForecastsForDigest(items: MutableList<StrictViolationForecast>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun derivePendingViolationDigestsFromForecast(items: MutableList<StrictViolationForecast>): MutableList<PendingViolationDigest> {
    val out = mutableListOf<PendingViolationDigest>()
    for (item in items) {
        val derived = newPendingViolationDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun mergePendingViolationDigest(item: PendingViolationDigest): Double = strictViolationForecastWeight(9, true)
