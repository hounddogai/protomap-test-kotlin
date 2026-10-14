package com.acme.transport.network.zone.review

import com.acme.transport.network.cutoff.audit.DefaultCutoffAudit
import com.acme.transport.network.cutoff.audit.normalizeDefaultCutoffAuditCode

fun summarizeDefaultCutoffAuditsForLedger(items: MutableList<DefaultCutoffAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveManualZoneLedgersFromAudit(items: MutableList<DefaultCutoffAudit>): MutableList<ManualZoneLedger> {
    val out = mutableListOf<ManualZoneLedger>()
    for (item in items) {
        val derived = newManualZoneLedger(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyManualZoneLedger(item: ManualZoneLedger): String = normalizeDefaultCutoffAuditCode(item.code)
