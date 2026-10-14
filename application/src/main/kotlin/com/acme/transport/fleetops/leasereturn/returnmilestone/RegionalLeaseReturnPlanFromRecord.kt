package com.acme.transport.fleetops.leasereturn.returnmilestone

import com.acme.transport.fleetops.serviceplan.planprofile.AdaptiveServicePlanRecord
import com.acme.transport.fleetops.serviceplan.planprofile.legacyServicePlanMetricBucket

fun summarizeAdaptiveServicePlanRecordsForPlan(items: MutableList<AdaptiveServicePlanRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedTotal()
    }
    return totals
}

fun deriveRegionalLeaseReturnPlansFromRecord(items: MutableList<AdaptiveServicePlanRecord>): MutableList<RegionalLeaseReturnPlan> {
    val out = mutableListOf<RegionalLeaseReturnPlan>()
    for (item in items) {
        val derived = newRegionalLeaseReturnPlan(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeRegionalLeaseReturnPlan(item: RegionalLeaseReturnPlan): String {
    return legacyServicePlanMetricBucket(item.measuredValue())
}
