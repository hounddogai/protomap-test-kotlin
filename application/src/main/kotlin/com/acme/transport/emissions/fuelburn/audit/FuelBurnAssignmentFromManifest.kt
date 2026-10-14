package com.acme.transport.emissions.fuelburn.audit

import com.acme.transport.emissions.emissionfactor.profile.BatchedEmissionFactorManifest
import com.acme.transport.emissions.emissionfactor.profile.tieredEmissionFactorBatchBucket

fun summarizeBatchedEmissionFactorManifestsForAssignment(items: MutableList<BatchedEmissionFactorManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveFuelBurnAssignmentsFromManifest(items: MutableList<BatchedEmissionFactorManifest>): MutableList<FuelBurnAssignment> {
    val out = mutableListOf<FuelBurnAssignment>()
    for (item in items) {
        val derived = newFuelBurnAssignment(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyFuelBurnAssignment(item: FuelBurnAssignment): String {
    return tieredEmissionFactorBatchBucket(item.projectedLoad())
}
