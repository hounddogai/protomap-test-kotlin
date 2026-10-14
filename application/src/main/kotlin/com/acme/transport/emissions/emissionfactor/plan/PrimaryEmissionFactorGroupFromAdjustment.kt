package com.acme.transport.emissions.emissionfactor.plan

import com.acme.transport.emissions.emissionfactor.assignment.EmissionFactorAdjustment
import com.acme.transport.emissions.emissionfactor.assignment.classifyEmissionFactorAudit

fun summarizeEmissionFactorAdjustmentsForGroup(items: MutableList<EmissionFactorAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryEmissionFactorGroupsFromAdjustment(items: MutableList<EmissionFactorAdjustment>): MutableList<PrimaryEmissionFactorGroup> {
    val out = mutableListOf<PrimaryEmissionFactorGroup>()
    for (item in items) {
        val derived = newPrimaryEmissionFactorGroup(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergePrimaryEmissionFactorGroup(item: PrimaryEmissionFactorGroup): String {
    return classifyEmissionFactorAudit(item.adjustedLoad())
}
