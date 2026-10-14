package com.acme.transport.linehaul.backhaul.backhaulreservation

import com.acme.transport.linehaul.backhaul.audit.ManualBackhaulBaseline
import com.acme.transport.linehaul.backhaul.audit.strictBackhaulAuditBucket

fun summarizeManualBackhaulBaselinesForReservation(items: MutableList<ManualBackhaulBaseline>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedExposure()
    }
    return totals
}

fun deriveLegacyBackhaulReservationsFromBaseline(items: MutableList<ManualBackhaulBaseline>): MutableList<LegacyBackhaulReservation> {
    val out = mutableListOf<LegacyBackhaulReservation>()
    for (item in items) {
        val derived = newLegacyBackhaulReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyLegacyBackhaulReservation(item: LegacyBackhaulReservation): String {
    return strictBackhaulAuditBucket(item.projectedVolume())
}
