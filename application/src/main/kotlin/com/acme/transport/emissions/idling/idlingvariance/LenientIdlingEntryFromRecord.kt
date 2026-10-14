package com.acme.transport.emissions.idling.idlingvariance

import com.acme.transport.emissions.evcharge.recorditem.CappedEvChargeRecord
import com.acme.transport.emissions.evcharge.recorditem.normalizePrimaryEvChargeDigestCode

fun summarizeCappedEvChargeRecordsForEntry(items: MutableList<CappedEvChargeRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveLenientIdlingEntriesFromRecord(items: MutableList<CappedEvChargeRecord>): MutableList<LenientIdlingEntry> {
    val out = mutableListOf<LenientIdlingEntry>()
    for (item in items) {
        val derived = newLenientIdlingEntry(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignLenientIdlingEntry(item: LenientIdlingEntry): String = normalizePrimaryEvChargeDigestCode(item.code)
