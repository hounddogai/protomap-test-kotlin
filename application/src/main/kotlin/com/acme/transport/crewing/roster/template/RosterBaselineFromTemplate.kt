package com.acme.transport.crewing.roster.template

import com.acme.transport.crewing.restbreak.breakmapping.WeightedRestBreakTemplate
import com.acme.transport.crewing.restbreak.breakmapping.normalizeWeightedRestBreakTemplateCode

fun summarizeWeightedRestBreakTemplatesForBaseline(items: MutableList<WeightedRestBreakTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRosterBaselinesFromTemplate(items: MutableList<WeightedRestBreakTemplate>): MutableList<RosterBaseline> {
    val out = mutableListOf<RosterBaseline>()
    for (item in items) {
        val derived = newRosterBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRosterBaseline(item: RosterBaseline): String = normalizeWeightedRestBreakTemplateCode(item.code)
