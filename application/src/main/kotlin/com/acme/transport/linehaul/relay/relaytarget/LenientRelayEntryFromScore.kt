package com.acme.transport.linehaul.relay.relaytarget

import com.acme.transport.linehaul.relay.variance.LenientRelayScore
import com.acme.transport.linehaul.relay.variance.defaultRelayVarianceWeight

fun summarizeLenientRelayScoresForEntry(items: MutableList<LenientRelayScore>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveLenientRelayEntriesFromScore(items: MutableList<LenientRelayScore>): MutableList<LenientRelayEntry> {
    val out = mutableListOf<LenientRelayEntry>()
    for (item in items) {
        val derived = newLenientRelayEntry(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateLenientRelayEntry(item: LenientRelayEntry): Double = defaultRelayVarianceWeight(8, true)
