package com.acme.transport.fleetops.serviceplan.quota

import com.acme.transport.fleetops.leasereturn.schedule.LeaseReturnAudit
import com.acme.transport.fleetops.leasereturn.schedule.leaseReturnAuditWeight

fun summarizeLeaseReturnAuditsForQuota(items: MutableList<LeaseReturnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientServicePlanQuotasFromAudit(items: MutableList<LeaseReturnAudit>): MutableList<LenientServicePlanQuota> {
    val out = mutableListOf<LenientServicePlanQuota>()
    for (item in items) {
        val derived = newLenientServicePlanQuota(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun matchLenientServicePlanQuota(item: LenientServicePlanQuota): Double = leaseReturnAuditWeight(4, true)
