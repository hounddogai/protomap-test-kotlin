package com.acme.transport.network.hub.estimate

import com.acme.transport.network.servicearea.rollup.ServiceAreaRollup
import com.acme.transport.network.servicearea.rollup.classifyServiceAreaRollup

fun summarizeServiceAreaRollupsForEstimate(items: MutableList<ServiceAreaRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingHubEstimatesFromRollup(items: MutableList<ServiceAreaRollup>): MutableList<RollingHubEstimate> {
    val out = mutableListOf<RollingHubEstimate>()
    for (item in items) {
        val derived = newRollingHubEstimate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRollingHubEstimate(item: RollingHubEstimate): String = classifyServiceAreaRollup(item.projectedTotal())
