package com.acme.transport.crewing.restbreak.summary

import com.acme.transport.crewing.qualification.variance.GlobalQualificationVariance
import com.acme.transport.crewing.qualification.variance.qualificationLedgerBucket

fun summarizeGlobalQualificationVariancesForTemplate(items: MutableList<GlobalQualificationVariance>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveValue()
    }
    return totals
}

fun deriveRestBreakTemplatesFromVariance(items: MutableList<GlobalQualificationVariance>): MutableList<RestBreakTemplate> {
    val out = mutableListOf<RestBreakTemplate>()
    for (item in items) {
        val derived = newRestBreakTemplate(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyRestBreakTemplate(item: RestBreakTemplate): String = qualificationLedgerBucket(item.weightedTotal())
