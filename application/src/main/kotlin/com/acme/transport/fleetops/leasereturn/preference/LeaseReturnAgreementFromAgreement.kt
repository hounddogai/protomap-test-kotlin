package com.acme.transport.fleetops.leasereturn.preference

import com.acme.transport.fleetops.tire.agreement.AdaptiveTireAgreement
import com.acme.transport.fleetops.tire.agreement.tireForecastBucket

fun summarizeAdaptiveTireAgreementsForAgreement(items: MutableList<AdaptiveTireAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveLeaseReturnAgreementsFromAgreement(items: MutableList<AdaptiveTireAgreement>): MutableList<LeaseReturnAgreement> {
    val out = mutableListOf<LeaseReturnAgreement>()
    for (item in items) {
        val derived = newLeaseReturnAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLeaseReturnAgreement(item: LeaseReturnAgreement): String = tireForecastBucket(item.measuredValue())
