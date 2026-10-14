package com.acme.transport.emissions.emissionfactor.proposal

import com.acme.transport.emissions.idling.regionalidlingmanifest.RegionalIdlingException
import com.acme.transport.emissions.idling.regionalidlingmanifest.classifyFallbackIdlingManifest

fun summarizeRegionalIdlingExceptionsForProposal(items: MutableList<RegionalIdlingException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveGlobalEmissionFactorProposalsFromException(items: MutableList<RegionalIdlingException>): MutableList<GlobalEmissionFactorProposal> {
    val out = mutableListOf<GlobalEmissionFactorProposal>()
    for (item in items) {
        val derived = newGlobalEmissionFactorProposal(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeGlobalEmissionFactorProposal(item: GlobalEmissionFactorProposal): String {
    return classifyFallbackIdlingManifest(item.effectiveTotal())
}
