package com.acme.transport.linehaul.backhaul.audit

import com.acme.transport.linehaul.backhaul.allocation.ScheduledBackhaulPolicy
import com.acme.transport.linehaul.backhaul.allocation.backhaulDraftBucket

fun summarizeScheduledBackhaulPoliciesForAudit(items: MutableList<ScheduledBackhaulPolicy>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveBackhaulAuditsFromPolicy(items: MutableList<ScheduledBackhaulPolicy>): MutableList<BackhaulAudit> {
    val out = mutableListOf<BackhaulAudit>()
    for (item in items) {
        val derived = newBackhaulAudit(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun planBackhaulAudit(item: BackhaulAudit): String = backhaulDraftBucket(item.effectiveVolume())
