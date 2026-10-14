package com.acme.transport.customs.hold.recorditem

import com.acme.transport.customs.tariff.tariffagreement.StrictTariffScore
import com.acme.transport.customs.tariff.tariffagreement.normalizeStrictTariffScoreCode

fun summarizeStrictTariffScoresForRecord(items: MutableList<StrictTariffScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveHoldRecordsFromScore(items: MutableList<StrictTariffScore>): MutableList<HoldRecord> {
    val out = mutableListOf<HoldRecord>()
    for (item in items) {
        val derived = newHoldRecord(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileHoldRecord(item: HoldRecord): String = normalizeStrictTariffScoreCode(item.code)
