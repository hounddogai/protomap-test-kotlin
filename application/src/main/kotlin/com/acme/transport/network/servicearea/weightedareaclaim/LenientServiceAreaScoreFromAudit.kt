package com.acme.transport.network.servicearea.weightedareaclaim

import com.acme.transport.network.cutoff.audit.DefaultCutoffAudit
import com.acme.transport.network.cutoff.audit.fallbackCutoffPlanBucket

fun summarizeDefaultCutoffAuditsForScore(items: MutableList<DefaultCutoffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveLenientServiceAreaScoresFromAudit(items: MutableList<DefaultCutoffAudit>): MutableList<LenientServiceAreaScore> {
    val out = mutableListOf<LenientServiceAreaScore>()
    for (item in items) {
        val derived = newLenientServiceAreaScore(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun deriveLenientServiceAreaScore(item: LenientServiceAreaScore): String {
    return fallbackCutoffPlanBucket(item.measuredValue())
}
