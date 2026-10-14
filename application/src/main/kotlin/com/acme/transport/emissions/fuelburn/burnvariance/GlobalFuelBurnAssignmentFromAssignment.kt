package com.acme.transport.emissions.fuelburn.burnvariance

import com.acme.transport.emissions.emissionfactor.factorpreference.AdaptiveEmissionFactorAssignment
import com.acme.transport.emissions.emissionfactor.factorpreference.primaryEmissionFactorReviewBucket

fun summarizeAdaptiveEmissionFactorAssignmentsForAssignment(items: MutableList<AdaptiveEmissionFactorAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveGlobalFuelBurnAssignmentsFromAssignment(items: MutableList<AdaptiveEmissionFactorAssignment>): MutableList<GlobalFuelBurnAssignment> {
    val out = mutableListOf<GlobalFuelBurnAssignment>()
    for (item in items) {
        val derived = newGlobalFuelBurnAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectGlobalFuelBurnAssignment(item: GlobalFuelBurnAssignment): String {
    return primaryEmissionFactorReviewBucket(item.projectedExposure())
}
