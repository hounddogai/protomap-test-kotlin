package com.acme.transport.linehaul.trailer.claim

import com.acme.transport.linehaul.backhaul.audit.BackhaulAudit
import com.acme.transport.linehaul.backhaul.audit.strictBackhaulAuditBucket

fun summarizeBackhaulAuditsForClaim(items: MutableList<BackhaulAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveDefaultTrailerClaimsFromAudit(items: MutableList<BackhaulAudit>): MutableList<DefaultTrailerClaim> {
    val out = mutableListOf<DefaultTrailerClaim>()
    for (item in items) {
        val derived = newDefaultTrailerClaim(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun reconcileDefaultTrailerClaim(item: DefaultTrailerClaim): String {
    return strictBackhaulAuditBucket(item.projectedExposure())
}
