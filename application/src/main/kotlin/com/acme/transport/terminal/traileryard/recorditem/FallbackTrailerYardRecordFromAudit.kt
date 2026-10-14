package com.acme.transport.terminal.traileryard.recorditem

import com.acme.transport.terminal.crossdock.audit.GlobalCrossDockAudit
import com.acme.transport.terminal.crossdock.audit.classifyGlobalCrossDockAudit

fun summarizeGlobalCrossDockAuditsForRecord(items: MutableList<GlobalCrossDockAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedTotal()
    }
    return totals
}

fun deriveFallbackTrailerYardRecordsFromAudit(items: MutableList<GlobalCrossDockAudit>): MutableList<FallbackTrailerYardRecord> {
    val out = mutableListOf<FallbackTrailerYardRecord>()
    for (item in items) {
        val derived = newFallbackTrailerYardRecord(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeFallbackTrailerYardRecord(item: FallbackTrailerYardRecord): String {
    return classifyGlobalCrossDockAudit(item.measuredValue())
}
