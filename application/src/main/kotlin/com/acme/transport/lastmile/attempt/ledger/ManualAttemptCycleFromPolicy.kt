package com.acme.transport.lastmile.attempt.ledger

import com.acme.transport.lastmile.attempt.defaultattemptpolicy.LegacyAttemptPolicy
import com.acme.transport.lastmile.attempt.defaultattemptpolicy.normalizeCappedAttemptSlotCode

fun summarizeLegacyAttemptPoliciesForCycle(items: MutableList<LegacyAttemptPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveManualAttemptCyclesFromPolicy(items: MutableList<LegacyAttemptPolicy>): MutableList<ManualAttemptCycle> {
    val out = mutableListOf<ManualAttemptCycle>()
    for (item in items) {
        val derived = newManualAttemptCycle(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun scoreManualAttemptCycle(item: ManualAttemptCycle): String = normalizeCappedAttemptSlotCode(item.code)
