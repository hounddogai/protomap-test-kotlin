package com.acme.transport.crewing.dutyperiod.variance

import com.acme.transport.crewing.availability.scheduledavailabilitytier.BatchedAvailabilityTier
import com.acme.transport.crewing.availability.scheduledavailabilitytier.classifyBatchedAvailabilityTier

fun summarizeBatchedAvailabilityTiersForPreference(items: MutableList<BatchedAvailabilityTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun derivePendingDutyPeriodPreferencesFromTier(items: MutableList<BatchedAvailabilityTier>): MutableList<PendingDutyPeriodPreference> {
    val out = mutableListOf<PendingDutyPeriodPreference>()
    for (item in items) {
        val derived = newPendingDutyPeriodPreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun estimatePendingDutyPeriodPreference(item: PendingDutyPeriodPreference): String {
    return classifyBatchedAvailabilityTier(item.measuredValue())
}
