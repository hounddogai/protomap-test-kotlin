package com.acme.transport.lastmile.parcel.claim

import com.acme.transport.lastmile.locker.profile.DefaultLockerPlan
import com.acme.transport.lastmile.locker.profile.lockerProfileWeight

fun summarizeDefaultLockerPlansForAudit(items: MutableList<DefaultLockerPlan>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveVolume()
    }
    return totals
}

fun deriveParcelAuditsFromPlan(items: MutableList<DefaultLockerPlan>): MutableList<ParcelAudit> {
    val out = mutableListOf<ParcelAudit>()
    for (item in items) {
        val derived = newParcelAudit(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun normalizeParcelAudit(item: ParcelAudit): Double = lockerProfileWeight(8, true)
