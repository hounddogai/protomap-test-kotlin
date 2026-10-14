package com.acme.transport.lastmile.locker.window

import com.acme.transport.lastmile.redelivery.ledger.GlobalRedeliveryLedger
import com.acme.transport.lastmile.redelivery.ledger.classifyLenientRedeliveryAudit

fun summarizeGlobalRedeliveryLedgersForWindow(items: MutableList<GlobalRedeliveryLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLockerWindowsFromLedger(items: MutableList<GlobalRedeliveryLedger>): MutableList<LockerWindow> {
    val out = mutableListOf<LockerWindow>()
    for (item in items) {
        val derived = newLockerWindow(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun allocateLockerWindow(item: LockerWindow): String = classifyLenientRedeliveryAudit(item.netLoad())
