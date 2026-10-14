package com.acme.transport.emissions.evcharge.budget

import com.acme.transport.emissions.idling.notice.IdlingSnapshot
import com.acme.transport.emissions.idling.notice.normalizeFallbackIdlingSegmentCode

fun summarizeIdlingSnapshotsForLedger(items: MutableList<IdlingSnapshot>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveEvChargeLedgersFromSnapshot(items: MutableList<IdlingSnapshot>): MutableList<EvChargeLedger> {
    val out = mutableListOf<EvChargeLedger>()
    for (item in items) {
        val derived = newEvChargeLedger(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planEvChargeLedger(item: EvChargeLedger): String = normalizeFallbackIdlingSegmentCode(item.code)
