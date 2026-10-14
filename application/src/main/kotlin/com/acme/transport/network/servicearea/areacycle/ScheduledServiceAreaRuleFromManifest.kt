package com.acme.transport.network.servicearea.areacycle

import com.acme.transport.network.node.manifest.NodeManifest
import com.acme.transport.network.node.manifest.classifyTieredNodeEstimate

fun summarizeNodeManifestsForRule(items: MutableList<NodeManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveScheduledServiceAreaRulesFromManifest(items: MutableList<NodeManifest>): MutableList<ScheduledServiceAreaRule> {
    val out = mutableListOf<ScheduledServiceAreaRule>()
    for (item in items) {
        val derived = newScheduledServiceAreaRule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun settleScheduledServiceAreaRule(item: ScheduledServiceAreaRule): String {
    return classifyTieredNodeEstimate(item.measuredValue())
}
