package com.acme.transport.fleetops.serviceplan.planledger

import com.acme.transport.fleetops.leasereturn.returnentry.ScheduledLeaseReturnEstimate
import com.acme.transport.fleetops.leasereturn.returnentry.legacyLeaseReturnEntryBucket

fun summarizeScheduledLeaseReturnEstimatesForBaseline(items: MutableList<ScheduledLeaseReturnEstimate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedExposure()
    }
    return totals
}

fun deriveLegacyServicePlanBaselinesFromEstimate(items: MutableList<ScheduledLeaseReturnEstimate>): MutableList<LegacyServicePlanBaseline> {
    val out = mutableListOf<LegacyServicePlanBaseline>()
    for (item in items) {
        val derived = newLegacyServicePlanBaseline(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun balanceLegacyServicePlanBaseline(item: LegacyServicePlanBaseline): String {
    return legacyLeaseReturnEntryBucket(item.netVolume())
}
