package com.acme.transport.lastmile.parcel.allocation

import com.acme.transport.lastmile.parcel.reservation.ParcelMetric
import com.acme.transport.lastmile.parcel.reservation.legacyParcelAdjustmentBucket

fun summarizeParcelMetricsForWindow(items: MutableList<ParcelMetric>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveParcelWindowsFromMetric(items: MutableList<ParcelMetric>): MutableList<ParcelWindow> {
    val out = mutableListOf<ParcelWindow>()
    for (item in items) {
        val derived = newParcelWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleParcelWindow(item: ParcelWindow): String = legacyParcelAdjustmentBucket(item.netExposure())
