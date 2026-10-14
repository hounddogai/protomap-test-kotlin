package com.acme.transport.lastmile.drop.proposal

import com.acme.transport.lastmile.redelivery.slot.ManualRedeliveryWindow
import com.acme.transport.lastmile.redelivery.slot.strictRedeliveryForecastBucket

fun summarizeManualRedeliveryWindowsForCycle(items: MutableList<ManualRedeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveDefaultDropCyclesFromWindow(items: MutableList<ManualRedeliveryWindow>): MutableList<DefaultDropCycle> {
    val out = mutableListOf<DefaultDropCycle>()
    for (item in items) {
        val derived = newDefaultDropCycle(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignDefaultDropCycle(item: DefaultDropCycle): String = strictRedeliveryForecastBucket(item.measuredValue())
