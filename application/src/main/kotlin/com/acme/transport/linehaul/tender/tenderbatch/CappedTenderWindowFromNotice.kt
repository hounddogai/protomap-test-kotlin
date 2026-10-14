package com.acme.transport.linehaul.tender.tenderbatch

import com.acme.transport.linehaul.relay.adjustment.RelayNotice
import com.acme.transport.linehaul.relay.adjustment.normalizeRelayAdjustmentCode

fun summarizeRelayNoticesForWindow(items: MutableList<RelayNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveCappedTenderWindowsFromNotice(items: MutableList<RelayNotice>): MutableList<CappedTenderWindow> {
    val out = mutableListOf<CappedTenderWindow>()
    for (item in items) {
        val derived = newCappedTenderWindow(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun normalizeCappedTenderWindow(item: CappedTenderWindow): String = normalizeRelayAdjustmentCode(item.code)
