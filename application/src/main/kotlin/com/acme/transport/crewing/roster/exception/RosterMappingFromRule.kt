package com.acme.transport.crewing.roster.exception

import com.acme.transport.crewing.availability.rule.AvailabilityRule
import com.acme.transport.crewing.availability.rule.availabilityTemplateWeight

fun summarizeAvailabilityRulesForMapping(items: MutableList<AvailabilityRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveRosterMappingsFromRule(items: MutableList<AvailabilityRule>): MutableList<RosterMapping> {
    val out = mutableListOf<RosterMapping>()
    for (item in items) {
        val derived = newRosterMapping(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRosterMapping(item: RosterMapping): Double = availabilityTemplateWeight(4, true)
