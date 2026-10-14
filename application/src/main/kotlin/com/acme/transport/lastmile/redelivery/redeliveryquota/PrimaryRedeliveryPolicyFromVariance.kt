package com.acme.transport.lastmile.redelivery.redeliveryquota

import com.acme.transport.lastmile.parcel.parcelvariance.DefaultParcelVariance
import com.acme.transport.lastmile.parcel.parcelvariance.normalizeParcelRecordCode

fun summarizeDefaultParcelVariancesForPolicy(items: MutableList<DefaultParcelVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun derivePrimaryRedeliveryPoliciesFromVariance(items: MutableList<DefaultParcelVariance>): MutableList<PrimaryRedeliveryPolicy> {
    val out = mutableListOf<PrimaryRedeliveryPolicy>()
    for (item in items) {
        val derived = newPrimaryRedeliveryPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun matchPrimaryRedeliveryPolicy(item: PrimaryRedeliveryPolicy): String = normalizeParcelRecordCode(item.code)
