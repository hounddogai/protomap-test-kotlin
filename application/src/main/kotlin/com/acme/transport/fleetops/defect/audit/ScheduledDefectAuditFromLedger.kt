package com.acme.transport.fleetops.defect.audit

import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.RollingLeaseReturnLedger
import com.acme.transport.fleetops.leasereturn.adaptivereturnledger.leaseReturnClaimWeight

fun summarizeRollingLeaseReturnLedgersForAudit(items: MutableList<RollingLeaseReturnLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveScheduledDefectAuditsFromLedger(items: MutableList<RollingLeaseReturnLedger>): MutableList<ScheduledDefectAudit> {
    val out = mutableListOf<ScheduledDefectAudit>()
    for (item in items) {
        val derived = newScheduledDefectAudit(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun resolveScheduledDefectAudit(item: ScheduledDefectAudit): Double = leaseReturnClaimWeight(7, true)
