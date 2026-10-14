package com.acme.transport.lastmile.parcel.window

import com.acme.transport.lastmile.redelivery.window.PendingRedeliveryWindow
import com.acme.transport.lastmile.redelivery.window.classifyPendingRedeliveryWindow

fun summarizePendingRedeliveryWindowsForTier(items: MutableList<PendingRedeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveParcelTiersFromWindow(items: MutableList<PendingRedeliveryWindow>): MutableList<ParcelTier> {
    val out = mutableListOf<ParcelTier>()
    for (item in items) {
        val derived = newParcelTier(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun classifyParcelTier(item: ParcelTier): String = classifyPendingRedeliveryWindow(item.effectiveExposure())
