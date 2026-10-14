package com.acme.transport.safety.nearmiss.baseline

import com.acme.transport.safety.nearmiss.claim.PrimaryNearMissAssignment
import com.acme.transport.safety.nearmiss.claim.classifyNearMissPolicy

fun summarizePrimaryNearMissAssignmentsForBaseline(items: MutableList<PrimaryNearMissAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveNearMissBaselinesFromAssignment(items: MutableList<PrimaryNearMissAssignment>): MutableList<NearMissBaseline> {
    val out = mutableListOf<NearMissBaseline>()
    for (item in items) {
        val derived = newNearMissBaseline(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectNearMissBaseline(item: NearMissBaseline): String = classifyNearMissPolicy(item.weightedVolume())
