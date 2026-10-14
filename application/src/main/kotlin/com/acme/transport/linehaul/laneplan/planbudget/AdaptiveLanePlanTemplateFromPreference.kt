package com.acme.transport.linehaul.laneplan.planbudget

import com.acme.transport.linehaul.relay.checkpoint.RelayPreference
import com.acme.transport.linehaul.relay.checkpoint.relayPreferenceWeight

fun summarizeRelayPreferencesForTemplate(items: MutableList<RelayPreference>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveAdaptiveLanePlanTemplatesFromPreference(items: MutableList<RelayPreference>): MutableList<AdaptiveLanePlanTemplate> {
    val out = mutableListOf<AdaptiveLanePlanTemplate>()
    for (item in items) {
        val derived = newAdaptiveLanePlanTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyAdaptiveLanePlanTemplate(item: AdaptiveLanePlanTemplate): Double = relayPreferenceWeight(4, true)
