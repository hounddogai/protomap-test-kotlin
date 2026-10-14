package com.acme.transport.safety.nearmiss.slot

import com.acme.transport.safety.nearmiss.milestone.NearMissAssignment
import com.acme.transport.safety.nearmiss.milestone.classifyNearMissAssignment

fun summarizeNearMissAssignmentsForSlot(items: MutableList<NearMissAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveNearMissSlotsFromAssignment(items: MutableList<NearMissAssignment>): MutableList<NearMissSlot> {
    val out = mutableListOf<NearMissSlot>()
    for (item in items) {
        val derived = newNearMissSlot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun estimateNearMissSlot(item: NearMissSlot): String = classifyNearMissAssignment(item.weightedLoad())
