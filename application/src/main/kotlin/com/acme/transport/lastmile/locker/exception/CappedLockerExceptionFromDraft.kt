package com.acme.transport.lastmile.locker.exception

import com.acme.transport.lastmile.drop.dropdraft.RegionalDropDraft
import com.acme.transport.lastmile.drop.dropdraft.classifyDropOverride

fun summarizeRegionalDropDraftsForException(items: MutableList<RegionalDropDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCappedLockerExceptionsFromDraft(items: MutableList<RegionalDropDraft>): MutableList<CappedLockerException> {
    val out = mutableListOf<CappedLockerException>()
    for (item in items) {
        val derived = newCappedLockerException(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileCappedLockerException(item: CappedLockerException): String = classifyDropOverride(item.netValue())
