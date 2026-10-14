package com.acme.transport.customs.hold.schedule

import com.acme.transport.customs.declaration.baseline.DeclarationMilestone
import com.acme.transport.customs.declaration.baseline.classifyDeclarationMilestone

fun summarizeDeclarationMilestonesForSchedule(items: MutableList<DeclarationMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveDefaultHoldSchedulesFromMilestone(items: MutableList<DeclarationMilestone>): MutableList<DefaultHoldSchedule> {
    val out = mutableListOf<DefaultHoldSchedule>()
    for (item in items) {
        val derived = newDefaultHoldSchedule(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateDefaultHoldSchedule(item: DefaultHoldSchedule): String {
    return classifyDeclarationMilestone(item.weightedValue())
}
