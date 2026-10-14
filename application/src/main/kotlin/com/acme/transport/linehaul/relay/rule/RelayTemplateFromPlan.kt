package com.acme.transport.linehaul.relay.rule

import com.acme.transport.linehaul.relay.target.RelayPlan
import com.acme.transport.linehaul.relay.target.classifyTieredRelayRecord

fun summarizeRelayPlansForTemplate(items: MutableList<RelayPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveRelayTemplatesFromPlan(items: MutableList<RelayPlan>): MutableList<RelayTemplate> {
    val out = mutableListOf<RelayTemplate>()
    for (item in items) {
        val derived = newRelayTemplate(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitRelayTemplate(item: RelayTemplate): String = classifyTieredRelayRecord(item.weightedValue())
