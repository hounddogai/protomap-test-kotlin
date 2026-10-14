package com.acme.transport.linehaul.laneplan.planrule

import com.acme.transport.linehaul.relay.segment.PendingRelaySegment
import com.acme.transport.linehaul.relay.segment.classifyPendingRelayPreference

fun summarizePendingRelaySegmentsForProposal(items: MutableList<PendingRelaySegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLanePlanProposalsFromSegment(items: MutableList<PendingRelaySegment>): MutableList<LanePlanProposal> {
    val out = mutableListOf<LanePlanProposal>()
    for (item in items) {
        val derived = newLanePlanProposal(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimateLanePlanProposal(item: LanePlanProposal): String {
    return classifyPendingRelayPreference(item.projectedValue())
}
