package com.acme.transport.lastmile.redelivery.schedule

import com.acme.transport.lastmile.proofofdelivery.rollup.ScheduledProofOfDeliveryDigest
import com.acme.transport.lastmile.proofofdelivery.rollup.primaryProofOfDeliveryReservationBucket

fun summarizeScheduledProofOfDeliveryDigestsForSchedule(items: MutableList<ScheduledProofOfDeliveryDigest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netValue()
    }
    return totals
}

fun deriveSeasonalRedeliverySchedulesFromDigest(items: MutableList<ScheduledProofOfDeliveryDigest>): MutableList<SeasonalRedeliverySchedule> {
    val out = mutableListOf<SeasonalRedeliverySchedule>()
    for (item in items) {
        val derived = newSeasonalRedeliverySchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankSeasonalRedeliverySchedule(item: SeasonalRedeliverySchedule): String {
    return primaryProofOfDeliveryReservationBucket(item.effectiveExposure())
}
