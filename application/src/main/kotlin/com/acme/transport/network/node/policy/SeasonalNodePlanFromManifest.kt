package com.acme.transport.network.node.policy

import com.acme.transport.network.servicearea.manifest.ServiceAreaManifest
import com.acme.transport.network.servicearea.manifest.classifyServiceAreaManifest

fun summarizeServiceAreaManifestsForPlan(items: MutableList<ServiceAreaManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveSeasonalNodePlansFromManifest(items: MutableList<ServiceAreaManifest>): MutableList<SeasonalNodePlan> {
    val out = mutableListOf<SeasonalNodePlan>()
    for (item in items) {
        val derived = newSeasonalNodePlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun projectSeasonalNodePlan(item: SeasonalNodePlan): String = classifyServiceAreaManifest(item.netVolume())
