package com.acme.transport.lastmile.locker.lockerbudget

import com.acme.transport.lastmile.redelivery.slot.RedeliveryPlan
import com.acme.transport.lastmile.redelivery.slot.classifyLenientRedeliverySlot

fun summarizeRedeliveryPlansForSlot(items: MutableList<RedeliveryPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultLockerSlotsFromPlan(items: MutableList<RedeliveryPlan>): MutableList<DefaultLockerSlot> {
    val out = mutableListOf<DefaultLockerSlot>()
    for (item in items) {
        val derived = newDefaultLockerSlot(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectDefaultLockerSlot(item: DefaultLockerSlot): String {
    return classifyLenientRedeliverySlot(item.measuredValue())
}
