package com.acme.transport.emissions.idling.digest

import com.acme.transport.emissions.fuelburn.burnestimate.CappedFuelBurnMilestone
import com.acme.transport.emissions.fuelburn.burnestimate.normalizeCappedFuelBurnMilestoneCode

fun summarizeCappedFuelBurnMilestonesForAudit(items: MutableList<CappedFuelBurnMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveScheduledIdlingAuditsFromMilestone(items: MutableList<CappedFuelBurnMilestone>): MutableList<ScheduledIdlingAudit> {
    val out = mutableListOf<ScheduledIdlingAudit>()
    for (item in items) {
        val derived = newScheduledIdlingAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeScheduledIdlingAudit(item: ScheduledIdlingAudit): String {
    return normalizeCappedFuelBurnMilestoneCode(item.code)
}
