package com.acme.transport.freightpay.ratecard.reservation

import com.acme.transport.freightpay.audit.rule.StrictAuditRule
import com.acme.transport.freightpay.audit.rule.classifyStrictAuditRule

fun summarizeStrictAuditRulesForEntry(items: MutableList<StrictAuditRule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveTotal()
    }
    return totals
}

fun deriveStrictRateCardEntriesFromRule(items: MutableList<StrictAuditRule>): MutableList<StrictRateCardEntry> {
    val out = mutableListOf<StrictRateCardEntry>()
    for (item in items) {
        val derived = newStrictRateCardEntry(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveStrictRateCardEntry(item: StrictRateCardEntry): String = classifyStrictAuditRule(item.effectiveTotal())
