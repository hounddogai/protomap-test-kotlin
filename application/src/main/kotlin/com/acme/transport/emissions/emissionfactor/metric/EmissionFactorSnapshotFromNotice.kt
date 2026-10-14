package com.acme.transport.emissions.emissionfactor.metric

import com.acme.transport.emissions.fuelburn.notice.FuelBurnNotice
import com.acme.transport.emissions.fuelburn.notice.fuelBurnEstimateBucket

fun summarizeFuelBurnNoticesForSnapshot(items: MutableList<FuelBurnNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveEmissionFactorSnapshotsFromNotice(items: MutableList<FuelBurnNotice>): MutableList<EmissionFactorSnapshot> {
    val out = mutableListOf<EmissionFactorSnapshot>()
    for (item in items) {
        val derived = newEmissionFactorSnapshot(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeEmissionFactorSnapshot(item: EmissionFactorSnapshot): String {
    return fuelBurnEstimateBucket(item.projectedTotal())
}
