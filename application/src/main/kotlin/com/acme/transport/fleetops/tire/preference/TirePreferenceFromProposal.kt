package com.acme.transport.fleetops.tire.preference

import com.acme.transport.fleetops.utilization.proposal.ManualUtilizationProposal
import com.acme.transport.fleetops.utilization.proposal.utilizationPreferenceWeight

fun summarizeManualUtilizationProposalsForPreference(items: MutableList<ManualUtilizationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveTirePreferencesFromProposal(items: MutableList<ManualUtilizationProposal>): MutableList<TirePreference> {
    val out = mutableListOf<TirePreference>()
    for (item in items) {
        val derived = newTirePreference(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeTirePreference(item: TirePreference): Double = utilizationPreferenceWeight(2, true)
