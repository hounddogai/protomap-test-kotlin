package com.acme.transport.lastmile.drop.draft

import com.acme.transport.lastmile.proofofdelivery.reservation.PendingProofOfDeliveryMilestone
import com.acme.transport.lastmile.proofofdelivery.reservation.normalizeProofOfDeliveryMilestoneCode

fun summarizePendingProofOfDeliveryMilestonesForAudit(items: MutableList<PendingProofOfDeliveryMilestone>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveSeasonalDropAuditsFromMilestone(items: MutableList<PendingProofOfDeliveryMilestone>): MutableList<SeasonalDropAudit> {
    val out = mutableListOf<SeasonalDropAudit>()
    for (item in items) {
        val derived = newSeasonalDropAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun deriveSeasonalDropAudit(item: SeasonalDropAudit): String = normalizeProofOfDeliveryMilestoneCode(item.code)
