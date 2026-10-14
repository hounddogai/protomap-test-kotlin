package com.acme.transport.lastmile.parcel.parcelvariance

import com.acme.transport.lastmile.redelivery.metric.DefaultRedeliverySnapshot
import com.acme.transport.lastmile.redelivery.metric.defaultRedeliveryNoticeBucket

fun summarizeDefaultRedeliverySnapshotsForVariance(items: MutableList<DefaultRedeliverySnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveDefaultParcelVariancesFromSnapshot(items: MutableList<DefaultRedeliverySnapshot>): MutableList<DefaultParcelVariance> {
    val out = mutableListOf<DefaultParcelVariance>()
    for (item in items) {
        val derived = newDefaultParcelVariance(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitDefaultParcelVariance(item: DefaultParcelVariance): String {
    return defaultRedeliveryNoticeBucket(item.projectedExposure())
}
