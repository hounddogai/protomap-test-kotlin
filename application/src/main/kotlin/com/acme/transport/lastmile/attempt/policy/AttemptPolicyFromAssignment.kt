package com.acme.transport.lastmile.attempt.policy

import com.acme.transport.lastmile.drop.allocation.DropAssignment
import com.acme.transport.lastmile.drop.allocation.normalizeRegionalDropAllocationCode

fun summarizeDropAssignmentsForPolicy(items: MutableList<DropAssignment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveAttemptPoliciesFromAssignment(items: MutableList<DropAssignment>): MutableList<AttemptPolicy> {
    val out = mutableListOf<AttemptPolicy>()
    for (item in items) {
        val derived = newAttemptPolicy(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectAttemptPolicy(item: AttemptPolicy): String = normalizeRegionalDropAllocationCode(item.code)
