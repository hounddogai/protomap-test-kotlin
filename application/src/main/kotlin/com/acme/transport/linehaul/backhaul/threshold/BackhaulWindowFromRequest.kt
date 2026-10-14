package com.acme.transport.linehaul.backhaul.threshold

import com.acme.transport.linehaul.backhaul.backhaulbatch.BackhaulRequest
import com.acme.transport.linehaul.backhaul.backhaulbatch.classifyBackhaulRequest

fun summarizeBackhaulRequestsForWindow(items: MutableList<BackhaulRequest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveBackhaulWindowsFromRequest(items: MutableList<BackhaulRequest>): MutableList<BackhaulWindow> {
    val out = mutableListOf<BackhaulWindow>()
    for (item in items) {
        val derived = newBackhaulWindow(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleBackhaulWindow(item: BackhaulWindow): String = classifyBackhaulRequest(item.projectedTotal())
