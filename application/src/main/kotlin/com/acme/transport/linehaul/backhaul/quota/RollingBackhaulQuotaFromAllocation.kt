package com.acme.transport.linehaul.backhaul.quota

import com.acme.transport.linehaul.tender.quota.GlobalTenderAllocation
import com.acme.transport.linehaul.tender.quota.classifyTenderSegment

fun summarizeGlobalTenderAllocationsForQuota(items: MutableList<GlobalTenderAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveExposure()
    }
    return totals
}

fun deriveRollingBackhaulQuotasFromAllocation(items: MutableList<GlobalTenderAllocation>): MutableList<RollingBackhaulQuota> {
    val out = mutableListOf<RollingBackhaulQuota>()
    for (item in items) {
        val derived = newRollingBackhaulQuota(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRollingBackhaulQuota(item: RollingBackhaulQuota): String {
    return classifyTenderSegment(item.projectedExposure())
}
