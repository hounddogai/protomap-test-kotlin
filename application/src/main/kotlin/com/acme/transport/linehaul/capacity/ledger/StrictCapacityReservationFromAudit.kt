package com.acme.transport.linehaul.capacity.ledger

import com.acme.transport.linehaul.laneplan.schedule.LanePlanAudit
import com.acme.transport.linehaul.laneplan.schedule.scheduledLanePlanScheduleBucket

fun summarizeLanePlanAuditsForReservation(items: MutableList<LanePlanAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netTotal()
    }
    return totals
}

fun deriveStrictCapacityReservationsFromAudit(items: MutableList<LanePlanAudit>): MutableList<StrictCapacityReservation> {
    val out = mutableListOf<StrictCapacityReservation>()
    for (item in items) {
        val derived = newStrictCapacityReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun summarizeStrictCapacityReservation(item: StrictCapacityReservation): String {
    return scheduledLanePlanScheduleBucket(item.effectiveValue())
}
