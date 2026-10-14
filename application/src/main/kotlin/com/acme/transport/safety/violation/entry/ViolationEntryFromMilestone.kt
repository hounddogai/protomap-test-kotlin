package com.acme.transport.safety.violation.entry

import com.acme.transport.safety.nearmiss.missrecord.PendingNearMissMilestone
import com.acme.transport.safety.nearmiss.missrecord.scheduledNearMissRecordBucket

fun summarizePendingNearMissMilestonesForEntry(items: MutableList<PendingNearMissMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveViolationEntriesFromMilestone(items: MutableList<PendingNearMissMilestone>): MutableList<ViolationEntry> {
    val out = mutableListOf<ViolationEntry>()
    for (item in items) {
        val derived = newViolationEntry(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun classifyViolationEntry(item: ViolationEntry): String = scheduledNearMissRecordBucket(item.measuredValue())
