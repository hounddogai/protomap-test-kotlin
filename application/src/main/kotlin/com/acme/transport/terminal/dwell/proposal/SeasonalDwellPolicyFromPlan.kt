package com.acme.transport.terminal.dwell.proposal

import com.acme.transport.terminal.dwell.dwellgroup.RollingDwellPlan
import com.acme.transport.terminal.dwell.dwellgroup.classifyRollingDwellPlan

fun summarizeRollingDwellPlansForPolicy(items: MutableList<RollingDwellPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveSeasonalDwellPoliciesFromPlan(items: MutableList<RollingDwellPlan>): MutableList<SeasonalDwellPolicy> {
    val out = mutableListOf<SeasonalDwellPolicy>()
    for (item in items) {
        val derived = newSeasonalDwellPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateSeasonalDwellPolicy(item: SeasonalDwellPolicy): String = classifyRollingDwellPlan(item.netTotal())
