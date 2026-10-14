package com.acme.transport.terminal.appointment.ledger

import com.acme.transport.terminal.crossdock.dockslot.LegacyCrossDockException
import com.acme.transport.terminal.crossdock.dockslot.normalizeCrossDockAdjustmentCode

fun summarizeLegacyCrossDockExceptionsForLedger(items: MutableList<LegacyCrossDockException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveLenientAppointmentLedgersFromException(items: MutableList<LegacyCrossDockException>): MutableList<LenientAppointmentLedger> {
    val out = mutableListOf<LenientAppointmentLedger>()
    for (item in items) {
        val derived = newLenientAppointmentLedger(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun splitLenientAppointmentLedger(item: LenientAppointmentLedger): String {
    return normalizeCrossDockAdjustmentCode(item.code)
}
