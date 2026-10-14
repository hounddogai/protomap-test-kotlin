package com.acme.transport.crewing.roster.rosterquota

import com.acme.transport.crewing.availability.mapping.RollingAvailabilityMapping
import com.acme.transport.crewing.availability.mapping.globalAvailabilityProposalWeight

fun summarizeRollingAvailabilityMappingsForRollup(items: MutableList<RollingAvailabilityMapping>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingRosterRollupsFromMapping(items: MutableList<RollingAvailabilityMapping>): MutableList<RollingRosterRollup> {
    val out = mutableListOf<RollingRosterRollup>()
    for (item in items) {
        val derived = newRollingRosterRollup(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeRollingRosterRollup(item: RollingRosterRollup): Double = globalAvailabilityProposalWeight(1, true)
