package com.acme.transport.fleetops.leasereturn.returnrequest

import com.acme.transport.fleetops.serviceplan.agreement.LenientServicePlanDigest
import com.acme.transport.fleetops.serviceplan.agreement.seasonalServicePlanAgreementWeight

fun summarizeLenientServicePlanDigestsForRequest(items: MutableList<LenientServicePlanDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLegacyLeaseReturnRequestsFromDigest(items: MutableList<LenientServicePlanDigest>): MutableList<LegacyLeaseReturnRequest> {
    val out = mutableListOf<LegacyLeaseReturnRequest>()
    for (item in items) {
        val derived = newLegacyLeaseReturnRequest(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun mergeLegacyLeaseReturnRequest(item: LegacyLeaseReturnRequest): Double {
    return seasonalServicePlanAgreementWeight(2, true)
}
