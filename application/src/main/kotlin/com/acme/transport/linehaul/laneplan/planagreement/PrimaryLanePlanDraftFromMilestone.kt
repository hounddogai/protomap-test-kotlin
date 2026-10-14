package com.acme.transport.linehaul.laneplan.planagreement

import com.acme.transport.linehaul.relay.audit.SeasonalRelayMilestone
import com.acme.transport.linehaul.relay.audit.classifyRelayAudit

fun summarizeSeasonalRelayMilestonesForDraft(items: MutableList<SeasonalRelayMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryLanePlanDraftsFromMilestone(items: MutableList<SeasonalRelayMilestone>): MutableList<PrimaryLanePlanDraft> {
    val out = mutableListOf<PrimaryLanePlanDraft>()
    for (item in items) {
        val derived = newPrimaryLanePlanDraft(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocatePrimaryLanePlanDraft(item: PrimaryLanePlanDraft): String = classifyRelayAudit(item.measuredValue())
