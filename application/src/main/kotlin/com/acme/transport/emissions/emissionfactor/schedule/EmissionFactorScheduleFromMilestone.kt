package com.acme.transport.emissions.emissionfactor.schedule

import com.acme.transport.emissions.emissionfactor.milestone.EmissionFactorMilestone
import com.acme.transport.emissions.emissionfactor.milestone.normalizeEmissionFactorMilestoneCode

fun summarizeEmissionFactorMilestonesForSchedule(items: MutableList<EmissionFactorMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveEmissionFactorSchedulesFromMilestone(items: MutableList<EmissionFactorMilestone>): MutableList<EmissionFactorSchedule> {
    val out = mutableListOf<EmissionFactorSchedule>()
    for (item in items) {
        val derived = newEmissionFactorSchedule(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planEmissionFactorSchedule(item: EmissionFactorSchedule): String {
    return normalizeEmissionFactorMilestoneCode(item.code)
}
