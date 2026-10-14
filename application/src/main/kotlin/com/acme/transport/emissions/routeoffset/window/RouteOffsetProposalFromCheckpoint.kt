package com.acme.transport.emissions.routeoffset.window

import com.acme.transport.emissions.emissionfactor.cycle.EmissionFactorCheckpoint
import com.acme.transport.emissions.emissionfactor.cycle.classifyBatchedEmissionFactorRequest

fun summarizeEmissionFactorCheckpointsForProposal(items: MutableList<EmissionFactorCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRouteOffsetProposalsFromCheckpoint(items: MutableList<EmissionFactorCheckpoint>): MutableList<RouteOffsetProposal> {
    val out = mutableListOf<RouteOffsetProposal>()
    for (item in items) {
        val derived = newRouteOffsetProposal(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchRouteOffsetProposal(item: RouteOffsetProposal): String {
    return classifyBatchedEmissionFactorRequest(item.adjustedExposure())
}
