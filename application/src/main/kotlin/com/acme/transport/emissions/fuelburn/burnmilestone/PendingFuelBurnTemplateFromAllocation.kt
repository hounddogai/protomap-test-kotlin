package com.acme.transport.emissions.fuelburn.burnmilestone

import com.acme.transport.emissions.fuelburn.burnvariance.ScheduledFuelBurnAllocation
import com.acme.transport.emissions.fuelburn.burnvariance.regionalFuelBurnRecordBucket

fun summarizeScheduledFuelBurnAllocationsForTemplate(items: MutableList<ScheduledFuelBurnAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePendingFuelBurnTemplatesFromAllocation(items: MutableList<ScheduledFuelBurnAllocation>): MutableList<PendingFuelBurnTemplate> {
    val out = mutableListOf<PendingFuelBurnTemplate>()
    for (item in items) {
        val derived = newPendingFuelBurnTemplate(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balancePendingFuelBurnTemplate(item: PendingFuelBurnTemplate): String {
    return regionalFuelBurnRecordBucket(item.netValue())
}
