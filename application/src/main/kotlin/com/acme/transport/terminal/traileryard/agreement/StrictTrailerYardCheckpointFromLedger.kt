package com.acme.transport.terminal.traileryard.agreement

import com.acme.transport.terminal.dwell.dwellpreference.DwellLedger
import com.acme.transport.terminal.dwell.dwellpreference.normalizeDwellLedgerCode

fun summarizeDwellLedgersForCheckpoint(items: MutableList<DwellLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveStrictTrailerYardCheckpointsFromLedger(items: MutableList<DwellLedger>): MutableList<StrictTrailerYardCheckpoint> {
    val out = mutableListOf<StrictTrailerYardCheckpoint>()
    for (item in items) {
        val derived = newStrictTrailerYardCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun reconcileStrictTrailerYardCheckpoint(item: StrictTrailerYardCheckpoint): String {
    return normalizeDwellLedgerCode(item.code)
}
