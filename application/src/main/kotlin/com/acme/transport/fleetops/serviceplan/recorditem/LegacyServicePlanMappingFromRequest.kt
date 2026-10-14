package com.acme.transport.fleetops.serviceplan.recorditem

import com.acme.transport.fleetops.leasereturn.returnrequest.LegacyLeaseReturnRequest
import com.acme.transport.fleetops.leasereturn.returnrequest.legacyLeaseReturnRequestBucket

fun summarizeLegacyLeaseReturnRequestsForMapping(items: MutableList<LegacyLeaseReturnRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveLegacyServicePlanMappingsFromRequest(items: MutableList<LegacyLeaseReturnRequest>): MutableList<LegacyServicePlanMapping> {
    val out = mutableListOf<LegacyServicePlanMapping>()
    for (item in items) {
        val derived = newLegacyServicePlanMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeLegacyServicePlanMapping(item: LegacyServicePlanMapping): String {
    return legacyLeaseReturnRequestBucket(item.weightedExposure())
}
