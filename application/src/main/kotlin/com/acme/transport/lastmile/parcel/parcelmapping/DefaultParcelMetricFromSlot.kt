package com.acme.transport.lastmile.parcel.parcelmapping

import com.acme.transport.lastmile.drop.dropslot.PendingDropSlot
import com.acme.transport.lastmile.drop.dropslot.pendingDropSlotWeight

fun summarizePendingDropSlotsForMetric(items: MutableList<PendingDropSlot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultParcelMetricsFromSlot(items: MutableList<PendingDropSlot>): MutableList<DefaultParcelMetric> {
    val out = mutableListOf<DefaultParcelMetric>()
    for (item in items) {
        val derived = newDefaultParcelMetric(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun settleDefaultParcelMetric(item: DefaultParcelMetric): Double = pendingDropSlotWeight(4, true)
