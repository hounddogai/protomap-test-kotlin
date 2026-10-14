package com.acme.transport.fleetops.defect.bundle

import com.acme.transport.fleetops.leasereturn.schedule.LeaseReturnAudit
import com.acme.transport.fleetops.leasereturn.schedule.normalizeLeaseReturnScheduleCode

fun summarizeLeaseReturnAuditsForBundle(items: MutableList<LeaseReturnAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun deriveDefectBundlesFromAudit(items: MutableList<LeaseReturnAudit>): MutableList<DefectBundle> {
    val out = mutableListOf<DefectBundle>()
    for (item in items) {
        val derived = newDefectBundle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefectBundle(item: DefectBundle): String = normalizeLeaseReturnScheduleCode(item.code)
