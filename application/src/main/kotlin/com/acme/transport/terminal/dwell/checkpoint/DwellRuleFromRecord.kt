package com.acme.transport.terminal.dwell.checkpoint

import com.acme.transport.terminal.crossdock.recorditem.CrossDockRecord
import com.acme.transport.terminal.crossdock.recorditem.crossDockScheduleWeight

fun summarizeCrossDockRecordsForRule(items: MutableList<CrossDockRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveDwellRulesFromRecord(items: MutableList<CrossDockRecord>): MutableList<DwellRule> {
    val out = mutableListOf<DwellRule>()
    for (item in items) {
        val derived = newDwellRule(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun resolveDwellRule(item: DwellRule): Double = crossDockScheduleWeight(9, true)
