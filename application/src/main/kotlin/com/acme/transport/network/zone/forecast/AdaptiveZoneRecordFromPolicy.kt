package com.acme.transport.network.zone.forecast

import com.acme.transport.network.node.nodepolicy.PrimaryNodePolicy
import com.acme.transport.network.node.nodepolicy.classifyNodeBundle

fun summarizePrimaryNodePoliciesForRecord(items: MutableList<PrimaryNodePolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveAdaptiveZoneRecordsFromPolicy(items: MutableList<PrimaryNodePolicy>): MutableList<AdaptiveZoneRecord> {
    val out = mutableListOf<AdaptiveZoneRecord>()
    for (item in items) {
        val derived = newAdaptiveZoneRecord(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveZoneRecord(item: AdaptiveZoneRecord): String = classifyNodeBundle(item.weightedExposure())
