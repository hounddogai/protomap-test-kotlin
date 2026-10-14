package com.acme.transport.fleetops.tire.preference

import com.acme.transport.fleetops.leasereturn.schedule.LeaseReturnAudit
import com.acme.transport.fleetops.leasereturn.schedule.normalizeLeaseReturnScheduleCode

fun summarizeLeaseReturnAuditsForReservation(items: MutableList<LeaseReturnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveRollingTireReservationsFromAudit(items: MutableList<LeaseReturnAudit>): MutableList<RollingTireReservation> {
    val out = mutableListOf<RollingTireReservation>()
    for (item in items) {
        val derived = newRollingTireReservation(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun applyRollingTireReservation(item: RollingTireReservation): String {
    return normalizeLeaseReturnScheduleCode(item.code)
}
