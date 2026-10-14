package com.acme.transport.crewing.restbreak.entry

import com.acme.transport.crewing.availability.audit.AvailabilityAudit
import com.acme.transport.crewing.availability.audit.normalizeLegacyAvailabilityGroupCode

fun summarizeAvailabilityAuditsForDraft(items: MutableList<AvailabilityAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveStrictRestBreakDraftsFromAudit(items: MutableList<AvailabilityAudit>): MutableList<StrictRestBreakDraft> {
    val out = mutableListOf<StrictRestBreakDraft>()
    for (item in items) {
        val derived = newStrictRestBreakDraft(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun resolveStrictRestBreakDraft(item: StrictRestBreakDraft): String {
    return normalizeLegacyAvailabilityGroupCode(item.code)
}
