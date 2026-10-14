package com.acme.transport.lastmile.redelivery.allocation

import com.acme.transport.lastmile.parcel.parcelbaseline.CappedParcelQuota
import com.acme.transport.lastmile.parcel.parcelbaseline.tieredParcelBaselineBucket

fun summarizeCappedParcelQuotasForTemplate(items: MutableList<CappedParcelQuota>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveRollingRedeliveryTemplatesFromQuota(items: MutableList<CappedParcelQuota>): MutableList<RollingRedeliveryTemplate> {
    val out = mutableListOf<RollingRedeliveryTemplate>()
    for (item in items) {
        val derived = newRollingRedeliveryTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveRollingRedeliveryTemplate(item: RollingRedeliveryTemplate): String {
    return tieredParcelBaselineBucket(item.effectiveVolume())
}
