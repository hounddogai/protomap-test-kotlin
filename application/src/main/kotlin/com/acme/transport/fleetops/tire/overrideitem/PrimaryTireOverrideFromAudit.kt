package com.acme.transport.fleetops.tire.overrideitem

import com.acme.transport.fleetops.leasereturn.returnaudit.RegionalLeaseReturnAudit
import com.acme.transport.fleetops.leasereturn.returnaudit.regionalLeaseReturnAuditBucket

fun summarizeRegionalLeaseReturnAuditsForOverride(items: MutableList<RegionalLeaseReturnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun derivePrimaryTireOverridesFromAudit(items: MutableList<RegionalLeaseReturnAudit>): MutableList<PrimaryTireOverride> {
    val out = mutableListOf<PrimaryTireOverride>()
    for (item in items) {
        val derived = newPrimaryTireOverride(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun splitPrimaryTireOverride(item: PrimaryTireOverride): String {
    return regionalLeaseReturnAuditBucket(item.weightedTotal())
}
