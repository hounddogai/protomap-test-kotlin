package com.acme.transport.linehaul.tender.reservation

import com.acme.transport.linehaul.laneplan.plancheckpoint.RegionalLanePlanCycle
import com.acme.transport.linehaul.laneplan.plancheckpoint.fallbackLanePlanCheckpointWeight

fun summarizeRegionalLanePlanCyclesForRollup(items: MutableList<RegionalLanePlanCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTieredTenderRollupsFromCycle(items: MutableList<RegionalLanePlanCycle>): MutableList<TieredTenderRollup> {
    val out = mutableListOf<TieredTenderRollup>()
    for (item in items) {
        val derived = newTieredTenderRollup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTieredTenderRollup(item: TieredTenderRollup): Double = fallbackLanePlanCheckpointWeight(7, true)
