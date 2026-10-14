package com.acme.transport.lastmile.parcel.plan

import com.acme.transport.lastmile.locker.snapshot.LockerAllocation
import com.acme.transport.lastmile.locker.snapshot.classifyLockerAllocation

fun summarizeLockerAllocationsForMapping(items: MutableList<LockerAllocation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveLenientParcelMappingsFromAllocation(items: MutableList<LockerAllocation>): MutableList<LenientParcelMapping> {
    val out = mutableListOf<LenientParcelMapping>()
    for (item in items) {
        val derived = newLenientParcelMapping(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun computeLenientParcelMapping(item: LenientParcelMapping): String {
    return classifyLockerAllocation(item.weightedLoad())
}
