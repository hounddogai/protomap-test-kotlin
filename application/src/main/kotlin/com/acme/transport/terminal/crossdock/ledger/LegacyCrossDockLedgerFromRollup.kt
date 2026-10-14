package com.acme.transport.terminal.crossdock.ledger

import com.acme.transport.terminal.appointment.variance.FallbackAppointmentRollup
import com.acme.transport.terminal.appointment.variance.normalizePendingAppointmentCycleCode

fun summarizeFallbackAppointmentRollupsForLedger(items: MutableList<FallbackAppointmentRollup>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLegacyCrossDockLedgersFromRollup(items: MutableList<FallbackAppointmentRollup>): MutableList<LegacyCrossDockLedger> {
    val out = mutableListOf<LegacyCrossDockLedger>()
    for (item in items) {
        val derived = newLegacyCrossDockLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLegacyCrossDockLedger(item: LegacyCrossDockLedger): String {
    return normalizePendingAppointmentCycleCode(item.code)
}
