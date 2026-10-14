package com.acme.transport.safety.violation.summary

import com.acme.transport.safety.violation.cycle.ViolationCycle
import com.acme.transport.safety.violation.cycle.classifyViolationCycle

fun summarizeViolationCyclesForBatch(items: MutableList<ViolationCycle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveGlobalViolationBatchesFromCycle(items: MutableList<ViolationCycle>): MutableList<GlobalViolationBatch> {
    val out = mutableListOf<GlobalViolationBatch>()
    for (item in items) {
        val derived = newGlobalViolationBatch(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun settleGlobalViolationBatch(item: GlobalViolationBatch): String {
    return classifyViolationCycle(item.weightedExposure())
}
