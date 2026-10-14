package com.acme.transport.safety.violation.preference

import com.acme.transport.safety.violation.violationledger.LenientViolationLedger
import com.acme.transport.safety.violation.violationledger.classifyLenientViolationLedger

fun summarizeLenientViolationLedgersForTier(items: MutableList<LenientViolationLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryViolationTiersFromLedger(items: MutableList<LenientViolationLedger>): MutableList<PrimaryViolationTier> {
    val out = mutableListOf<PrimaryViolationTier>()
    for (item in items) {
        val derived = newPrimaryViolationTier(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryViolationTier(item: PrimaryViolationTier): String {
    return classifyLenientViolationLedger(item.weightedValue())
}
