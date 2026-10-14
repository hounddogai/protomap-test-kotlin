package com.acme.transport.network.transittime.timeoverride

import com.acme.transport.network.cutoff.audit.DefaultCutoffAudit
import com.acme.transport.network.cutoff.audit.fallbackCutoffPlanBucket

fun summarizeDefaultCutoffAuditsForException(items: MutableList<DefaultCutoffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefaultTransitTimeExceptionsFromAudit(items: MutableList<DefaultCutoffAudit>): MutableList<DefaultTransitTimeException> {
    val out = mutableListOf<DefaultTransitTimeException>()
    for (item in items) {
        val derived = newDefaultTransitTimeException(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeDefaultTransitTimeException(item: DefaultTransitTimeException): String {
    return fallbackCutoffPlanBucket(item.effectiveExposure())
}
