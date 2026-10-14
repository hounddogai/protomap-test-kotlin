package com.acme.transport.network.cutoff.ledger

import com.acme.transport.network.node.agreement.RegionalNodeMapping
import com.acme.transport.network.node.agreement.regionalNodeMappingWeight

fun summarizeRegionalNodeMappingsForLedger(items: MutableList<RegionalNodeMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualCutoffLedgersFromMapping(items: MutableList<RegionalNodeMapping>): MutableList<ManualCutoffLedger> {
    val out = mutableListOf<ManualCutoffLedger>()
    for (item in items) {
        val derived = newManualCutoffLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun applyManualCutoffLedger(item: ManualCutoffLedger): Double = regionalNodeMappingWeight(5, true)
