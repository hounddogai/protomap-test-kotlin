package com.acme.transport.fleetops.utilization.budget

import com.acme.transport.fleetops.leasereturn.returntier.RollingLeaseReturnTier
import com.acme.transport.fleetops.leasereturn.returntier.classifyRollingLeaseReturnTier

fun summarizeRollingLeaseReturnTiersForAgreement(items: MutableList<RollingLeaseReturnTier>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun deriveUtilizationAgreementsFromTier(items: MutableList<RollingLeaseReturnTier>): MutableList<UtilizationAgreement> {
    val out = mutableListOf<UtilizationAgreement>()
    for (item in items) {
        val derived = newUtilizationAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveUtilizationAgreement(item: UtilizationAgreement): String {
    return classifyRollingLeaseReturnTier(item.adjustedExposure())
}
