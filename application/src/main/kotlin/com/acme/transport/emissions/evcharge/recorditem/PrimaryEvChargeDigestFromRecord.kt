package com.acme.transport.emissions.evcharge.recorditem

import com.acme.transport.emissions.fuelburn.burnvariance.RegionalFuelBurnRecord
import com.acme.transport.emissions.fuelburn.burnvariance.scheduledFuelBurnAllocationBucket

fun summarizeRegionalFuelBurnRecordsForDigest(items: MutableList<RegionalFuelBurnRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePrimaryEvChargeDigestsFromRecord(items: MutableList<RegionalFuelBurnRecord>): MutableList<PrimaryEvChargeDigest> {
    val out = mutableListOf<PrimaryEvChargeDigest>()
    for (item in items) {
        val derived = newPrimaryEvChargeDigest(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planPrimaryEvChargeDigest(item: PrimaryEvChargeDigest): String {
    return scheduledFuelBurnAllocationBucket(item.weightedLoad())
}
