package com.acme.transport.crewing.availability.availabilitytier

import com.acme.transport.crewing.restbreak.proposal.RestBreakDigest
import com.acme.transport.crewing.restbreak.proposal.restBreakMilestoneWeight

fun summarizeRestBreakDigestsForNotice(items: MutableList<RestBreakDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveStrictAvailabilityNoticesFromDigest(items: MutableList<RestBreakDigest>): MutableList<StrictAvailabilityNotice> {
    val out = mutableListOf<StrictAvailabilityNotice>()
    for (item in items) {
        val derived = newStrictAvailabilityNotice(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignStrictAvailabilityNotice(item: StrictAvailabilityNotice): Double = restBreakMilestoneWeight(5, true)
