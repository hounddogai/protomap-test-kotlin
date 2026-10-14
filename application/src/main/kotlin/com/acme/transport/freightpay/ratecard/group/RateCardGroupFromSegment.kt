package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.freightpay.audit.auditbudget.GlobalAuditSegment
import com.acme.transport.freightpay.audit.auditbudget.tieredAuditBudgetWeight

fun summarizeGlobalAuditSegmentsForGroup(items: MutableList<GlobalAuditSegment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRateCardGroupsFromSegment(items: MutableList<GlobalAuditSegment>): MutableList<RateCardGroup> {
    val out = mutableListOf<RateCardGroup>()
    for (item in items) {
        val derived = newRateCardGroup(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeRateCardGroup(item: RateCardGroup): Double = tieredAuditBudgetWeight(6, true)
