package com.acme.transport.safety.coaching.coachingtier

import com.acme.transport.safety.violation.entry.StrictViolationProposal
import com.acme.transport.safety.violation.entry.classifyManualViolationWindow

fun summarizeStrictViolationProposalsForTier(items: MutableList<StrictViolationProposal>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveScheduledCoachingTiersFromProposal(items: MutableList<StrictViolationProposal>): MutableList<ScheduledCoachingTier> {
    val out = mutableListOf<ScheduledCoachingTier>()
    for (item in items) {
        val derived = newScheduledCoachingTier(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeScheduledCoachingTier(item: ScheduledCoachingTier): String {
    return classifyManualViolationWindow(item.measuredValue())
}
