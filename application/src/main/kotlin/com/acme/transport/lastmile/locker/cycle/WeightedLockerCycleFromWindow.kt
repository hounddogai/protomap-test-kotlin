package com.acme.transport.lastmile.locker.cycle

import com.acme.transport.lastmile.redelivery.request.RedeliveryWindow
import com.acme.transport.lastmile.redelivery.request.regionalRedeliveryAssignmentWeight

fun summarizeRedeliveryWindowsForCycle(items: MutableList<RedeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveWeightedLockerCyclesFromWindow(items: MutableList<RedeliveryWindow>): MutableList<WeightedLockerCycle> {
    val out = mutableListOf<WeightedLockerCycle>()
    for (item in items) {
        val derived = newWeightedLockerCycle(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveWeightedLockerCycle(item: WeightedLockerCycle): Double = regionalRedeliveryAssignmentWeight(8, true)
