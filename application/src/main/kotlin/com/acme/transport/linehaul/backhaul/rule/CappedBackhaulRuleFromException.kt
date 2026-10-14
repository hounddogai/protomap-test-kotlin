package com.acme.transport.linehaul.backhaul.rule

import com.acme.transport.linehaul.laneplan.summary.CappedLanePlanException
import com.acme.transport.linehaul.laneplan.summary.normalizeScheduledLanePlanSummaryCode

fun summarizeCappedLanePlanExceptionsForRule(items: MutableList<CappedLanePlanException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveCappedBackhaulRulesFromException(items: MutableList<CappedLanePlanException>): MutableList<CappedBackhaulRule> {
    val out = mutableListOf<CappedBackhaulRule>()
    for (item in items) {
        val derived = newCappedBackhaulRule(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun splitCappedBackhaulRule(item: CappedBackhaulRule): String = normalizeScheduledLanePlanSummaryCode(item.code)
