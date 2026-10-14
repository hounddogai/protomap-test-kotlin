package com.acme.transport.fleetops.inspection.inspectiondraft

import com.acme.transport.fleetops.leasereturn.returnpreference.AdaptiveLeaseReturnProposal
import com.acme.transport.fleetops.leasereturn.returnpreference.pendingLeaseReturnPreferenceWeight

fun summarizeAdaptiveLeaseReturnProposalsForDraft(items: MutableList<AdaptiveLeaseReturnProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLegacyInspectionDraftsFromProposal(items: MutableList<AdaptiveLeaseReturnProposal>): MutableList<LegacyInspectionDraft> {
    val out = mutableListOf<LegacyInspectionDraft>()
    for (item in items) {
        val derived = newLegacyInspectionDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun assignLegacyInspectionDraft(item: LegacyInspectionDraft): Double = pendingLeaseReturnPreferenceWeight(2, true)
