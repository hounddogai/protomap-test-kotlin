package com.acme.transport.linehaul.tender.weightedtenderrollup

import com.acme.transport.linehaul.backhaul.request.AdaptiveBackhaulProposal
import com.acme.transport.linehaul.backhaul.request.rollingBackhaulBatchWeight

fun summarizeAdaptiveBackhaulProposalsForRollup(items: MutableList<AdaptiveBackhaulProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveRollingTenderRollupsFromProposal(items: MutableList<AdaptiveBackhaulProposal>): MutableList<RollingTenderRollup> {
    val out = mutableListOf<RollingTenderRollup>()
    for (item in items) {
        val derived = newRollingTenderRollup(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceRollingTenderRollup(item: RollingTenderRollup): Double = rollingBackhaulBatchWeight(6, true)
