package com.acme.transport.customs.hold.holdslot

import com.acme.transport.customs.declaration.notice.ScheduledDeclarationBaseline
import com.acme.transport.customs.declaration.notice.scheduledDeclarationCycleBucket

fun summarizeScheduledDeclarationBaselinesForSlot(items: MutableList<ScheduledDeclarationBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveLenientHoldSlotsFromBaseline(items: MutableList<ScheduledDeclarationBaseline>): MutableList<LenientHoldSlot> {
    val out = mutableListOf<LenientHoldSlot>()
    for (item in items) {
        val derived = newLenientHoldSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileLenientHoldSlot(item: LenientHoldSlot): String {
    return scheduledDeclarationCycleBucket(item.projectedTotal())
}
