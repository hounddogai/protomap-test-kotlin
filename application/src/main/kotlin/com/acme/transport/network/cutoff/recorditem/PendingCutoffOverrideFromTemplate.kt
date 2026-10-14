package com.acme.transport.network.cutoff.recorditem

import com.acme.transport.network.node.template.SeasonalNodeTemplate
import com.acme.transport.network.node.template.normalizeDefaultNodeAssignmentCode

fun summarizeSeasonalNodeTemplatesForOverride(items: MutableList<SeasonalNodeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun derivePendingCutoffOverridesFromTemplate(items: MutableList<SeasonalNodeTemplate>): MutableList<PendingCutoffOverride> {
    val out = mutableListOf<PendingCutoffOverride>()
    for (item in items) {
        val derived = newPendingCutoffOverride(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignPendingCutoffOverride(item: PendingCutoffOverride): String {
    return normalizeDefaultNodeAssignmentCode(item.code)
}
