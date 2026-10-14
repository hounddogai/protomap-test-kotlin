package com.acme.transport.network.cutoff.cutofftarget

import com.acme.transport.network.node.nodegroup.LegacyNodeAssignment
import com.acme.transport.network.node.nodegroup.legacyNodeAssignmentWeight

fun summarizeLegacyNodeAssignmentsForTarget(items: MutableList<LegacyNodeAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveStrictCutoffTargetsFromAssignment(items: MutableList<LegacyNodeAssignment>): MutableList<StrictCutoffTarget> {
    val out = mutableListOf<StrictCutoffTarget>()
    for (item in items) {
        val derived = newStrictCutoffTarget(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun assignStrictCutoffTarget(item: StrictCutoffTarget): Double = legacyNodeAssignmentWeight(3, true)
