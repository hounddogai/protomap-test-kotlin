package com.acme.transport.terminal.dwell.snapshot

import com.acme.transport.terminal.dwell.digest.DefaultDwellException
import com.acme.transport.terminal.dwell.digest.normalizeDefaultDwellExceptionCode

fun summarizeDefaultDwellExceptionsForSnapshot(items: MutableList<DefaultDwellException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveScheduledDwellSnapshotsFromException(items: MutableList<DefaultDwellException>): MutableList<ScheduledDwellSnapshot> {
    val out = mutableListOf<ScheduledDwellSnapshot>()
    for (item in items) {
        val derived = newScheduledDwellSnapshot(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveScheduledDwellSnapshot(item: ScheduledDwellSnapshot): String {
    return normalizeDefaultDwellExceptionCode(item.code)
}
