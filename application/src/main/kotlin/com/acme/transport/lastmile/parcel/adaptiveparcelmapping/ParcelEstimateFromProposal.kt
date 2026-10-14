package com.acme.transport.lastmile.parcel.adaptiveparcelmapping

import com.acme.transport.lastmile.parcel.quota.ManualParcelProposal
import com.acme.transport.lastmile.parcel.quota.normalizeManualParcelProposalCode

fun summarizeManualParcelProposalsForEstimate(items: MutableList<ManualParcelProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelEstimatesFromProposal(items: MutableList<ManualParcelProposal>): MutableList<ParcelEstimate> {
    val out = mutableListOf<ParcelEstimate>()
    for (item in items) {
        val derived = newParcelEstimate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyParcelEstimate(item: ParcelEstimate): String = normalizeManualParcelProposalCode(item.code)
