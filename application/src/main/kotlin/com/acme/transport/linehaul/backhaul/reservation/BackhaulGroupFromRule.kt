package com.acme.transport.linehaul.backhaul.reservation

import com.acme.transport.linehaul.tender.tenderbatch.TenderRule
import com.acme.transport.linehaul.tender.tenderbatch.normalizeSeasonalTenderBatchCode

fun summarizeTenderRulesForGroup(items: MutableList<TenderRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBackhaulGroupsFromRule(items: MutableList<TenderRule>): MutableList<BackhaulGroup> {
    val out = mutableListOf<BackhaulGroup>()
    for (item in items) {
        val derived = newBackhaulGroup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectBackhaulGroup(item: BackhaulGroup): String = normalizeSeasonalTenderBatchCode(item.code)
