package com.acme.transport.fleetops.defect.proposal

import com.acme.transport.fleetops.leasereturn.rule.LeaseReturnAdjustment
import com.acme.transport.fleetops.leasereturn.rule.normalizeLeaseReturnAdjustmentCode

fun summarizeLeaseReturnAdjustmentsForGroup(items: MutableList<LeaseReturnAdjustment>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netLoad()
    }
    return totals
}

fun deriveDefectGroupsFromAdjustment(items: MutableList<LeaseReturnAdjustment>): MutableList<DefectGroup> {
    val out = mutableListOf<DefectGroup>()
    for (item in items) {
        val derived = newDefectGroup(item.code + "-draft", item.label())
        out.add(derived)
    }
    return out
}

fun projectDefectGroup(item: DefectGroup): String = normalizeLeaseReturnAdjustmentCode(item.code)
