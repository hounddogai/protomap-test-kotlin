package com.acme.transport.freightpay.dispute.milestone

import com.acme.transport.freightpay.accessorial.plan.AdaptiveAccessorialCheckpoint
import com.acme.transport.freightpay.accessorial.plan.classifyLegacyAccessorialDraft

fun summarizeAdaptiveAccessorialCheckpointsForGroup(items: MutableList<AdaptiveAccessorialCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveDisputeGroupsFromCheckpoint(items: MutableList<AdaptiveAccessorialCheckpoint>): MutableList<DisputeGroup> {
    val out = mutableListOf<DisputeGroup>()
    for (item in items) {
        val derived = newDisputeGroup(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeDisputeGroup(item: DisputeGroup): String = classifyLegacyAccessorialDraft(item.adjustedVolume())
