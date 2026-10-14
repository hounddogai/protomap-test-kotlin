package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.lastmile.drop.target.AdaptiveDropLedger
import com.acme.transport.lastmile.drop.target.classifyGlobalDropSnapshot

fun summarizeAdaptiveDropLedgersForAssignment(items: MutableList<AdaptiveDropLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveCappedLockerAssignmentsFromLedger(items: MutableList<AdaptiveDropLedger>): MutableList<CappedLockerAssignment> {
    val out = mutableListOf<CappedLockerAssignment>()
    for (item in items) {
        val derived = newCappedLockerAssignment(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveCappedLockerAssignment(item: CappedLockerAssignment): String {
    return classifyGlobalDropSnapshot(item.netVolume())
}
