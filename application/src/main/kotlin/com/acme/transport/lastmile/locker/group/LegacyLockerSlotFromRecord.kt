package com.acme.transport.lastmile.locker.group

import com.acme.transport.lastmile.drop.recorditem.DropRecord
import com.acme.transport.lastmile.drop.recorditem.classifyGlobalDropException

fun summarizeDropRecordsForSlot(items: MutableList<DropRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveLegacyLockerSlotsFromRecord(items: MutableList<DropRecord>): MutableList<LegacyLockerSlot> {
    val out = mutableListOf<LegacyLockerSlot>()
    for (item in items) {
        val derived = newLegacyLockerSlot(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitLegacyLockerSlot(item: LegacyLockerSlot): String = classifyGlobalDropException(item.measuredValue())
