package com.acme.transport.crewing.qualification.rule

import com.acme.transport.crewing.restbreak.breakmilestone.StrictRestBreakCheckpoint
import com.acme.transport.crewing.restbreak.breakmilestone.strictRestBreakCheckpointBucket

fun summarizeStrictRestBreakCheckpointsForRule(items: MutableList<StrictRestBreakCheckpoint>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveRollingQualificationRulesFromCheckpoint(items: MutableList<StrictRestBreakCheckpoint>): MutableList<RollingQualificationRule> {
    val out = mutableListOf<RollingQualificationRule>()
    for (item in items) {
        val derived = newRollingQualificationRule(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveRollingQualificationRule(item: RollingQualificationRule): String {
    return strictRestBreakCheckpointBucket(item.effectiveValue())
}
