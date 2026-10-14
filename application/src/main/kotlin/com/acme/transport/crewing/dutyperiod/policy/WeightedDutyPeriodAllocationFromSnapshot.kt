package com.acme.transport.crewing.dutyperiod.policy

import com.acme.transport.crewing.dutyperiod.periodsnapshot.PendingDutyPeriodSnapshot
import com.acme.transport.crewing.dutyperiod.periodsnapshot.dutyPeriodMilestoneBucket

fun summarizePendingDutyPeriodSnapshotsForAllocation(items: MutableList<PendingDutyPeriodSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netVolume()
    }
    return totals
}

fun deriveWeightedDutyPeriodAllocationsFromSnapshot(items: MutableList<PendingDutyPeriodSnapshot>): MutableList<WeightedDutyPeriodAllocation> {
    val out = mutableListOf<WeightedDutyPeriodAllocation>()
    for (item in items) {
        val derived = newWeightedDutyPeriodAllocation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankWeightedDutyPeriodAllocation(item: WeightedDutyPeriodAllocation): String {
    return dutyPeriodMilestoneBucket(item.effectiveVolume())
}
