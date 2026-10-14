package com.acme.transport.crewing.qualification.audit

import com.acme.transport.crewing.restbreak.adjustment.RestBreakWindow
import com.acme.transport.crewing.restbreak.adjustment.restBreakWindowWeight

fun summarizeRestBreakWindowsForAudit(items: MutableList<RestBreakWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveQualificationAuditsFromWindow(items: MutableList<RestBreakWindow>): MutableList<QualificationAudit> {
    val out = mutableListOf<QualificationAudit>()
    for (item in items) {
        val derived = newQualificationAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun estimateQualificationAudit(item: QualificationAudit): Double = restBreakWindowWeight(4, true)
