package com.acme.transport.lastmile.drop.slot

import com.acme.transport.lastmile.redelivery.milestone.RedeliveryMilestone
import com.acme.transport.lastmile.redelivery.milestone.normalizeRedeliveryMilestoneCode

fun summarizeRedeliveryMilestonesForProfile(items: MutableList<RedeliveryMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveDropProfilesFromMilestone(items: MutableList<RedeliveryMilestone>): MutableList<DropProfile> {
    val out = mutableListOf<DropProfile>()
    for (item in items) {
        val derived = newDropProfile(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun classifyDropProfileExtra(item: DropProfile): String = normalizeRedeliveryMilestoneCode(item.code)
