package com.acme.transport.fleetops.tire.tirereservation

import com.acme.transport.fleetops.serviceplan.planpreference.WeightedServicePlanTemplate
import com.acme.transport.fleetops.serviceplan.planpreference.normalizeBatchedServicePlanSummaryCode

fun summarizeWeightedServicePlanTemplatesForProposal(items: MutableList<WeightedServicePlanTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRegionalTireProposalsFromTemplate(items: MutableList<WeightedServicePlanTemplate>): MutableList<RegionalTireProposal> {
    val out = mutableListOf<RegionalTireProposal>()
    for (item in items) {
        val derived = newRegionalTireProposal(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun matchRegionalTireProposal(item: RegionalTireProposal): String {
    return normalizeBatchedServicePlanSummaryCode(item.code)
}
