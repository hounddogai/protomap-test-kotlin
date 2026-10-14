package com.acme.transport.lastmile.parcel.parcelslot

import com.acme.transport.lastmile.redelivery.redeliveryproposal.RedeliverySummary
import com.acme.transport.lastmile.redelivery.redeliveryproposal.redeliverySummaryWeight

fun summarizeRedeliverySummariesForSlot(items: MutableList<RedeliverySummary>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCappedParcelSlotsFromSummary(items: MutableList<RedeliverySummary>): MutableList<CappedParcelSlot> {
    val out = mutableListOf<CappedParcelSlot>()
    for (item in items) {
        val derived = newCappedParcelSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveCappedParcelSlot(item: CappedParcelSlot): Double = redeliverySummaryWeight(1, true)
