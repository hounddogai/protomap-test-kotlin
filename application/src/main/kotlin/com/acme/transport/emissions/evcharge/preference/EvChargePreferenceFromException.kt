package com.acme.transport.emissions.evcharge.preference

import com.acme.transport.emissions.idling.metric.IdlingException
import com.acme.transport.emissions.idling.metric.idlingMetricWeight

fun summarizeIdlingExceptionsForPreference(items: MutableList<IdlingException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveEvChargePreferencesFromException(items: MutableList<IdlingException>): MutableList<EvChargePreference> {
    val out = mutableListOf<EvChargePreference>()
    for (item in items) {
        val derived = newEvChargePreference(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceEvChargePreference(item: EvChargePreference): Double = idlingMetricWeight(5, true)
