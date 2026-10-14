package com.acme.transport.emissions.emissionfactor.factormilestone

import com.acme.transport.emissions.routeoffset.ledger.GlobalRouteOffsetPlan
import com.acme.transport.emissions.routeoffset.ledger.classifyBatchedRouteOffsetMapping

fun summarizeGlobalRouteOffsetPlansForDraft(items: MutableList<GlobalRouteOffsetPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveCappedEmissionFactorDraftsFromPlan(items: MutableList<GlobalRouteOffsetPlan>): MutableList<CappedEmissionFactorDraft> {
    val out = mutableListOf<CappedEmissionFactorDraft>()
    for (item in items) {
        val derived = newCappedEmissionFactorDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedEmissionFactorDraft(item: CappedEmissionFactorDraft): String {
    return classifyBatchedRouteOffsetMapping(item.effectiveVolume())
}
