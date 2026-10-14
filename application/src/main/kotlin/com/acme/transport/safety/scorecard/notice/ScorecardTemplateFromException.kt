package com.acme.transport.safety.scorecard.notice

import com.acme.transport.safety.violation.exception.ViolationException
import com.acme.transport.safety.violation.exception.violationForecastBucket

fun summarizeViolationExceptionsForTemplate(items: MutableList<ViolationException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveScorecardTemplatesFromException(items: MutableList<ViolationException>): MutableList<ScorecardTemplate> {
    val out = mutableListOf<ScorecardTemplate>()
    for (item in items) {
        val derived = newScorecardTemplate(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun projectScorecardTemplate(item: ScorecardTemplate): String = violationForecastBucket(item.adjustedVolume())
