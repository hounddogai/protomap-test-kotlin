package com.acme.transport.crewing.dutyperiod.schedule

import com.acme.transport.crewing.restbreak.tier.PendingRestBreakAudit
import com.acme.transport.crewing.restbreak.tier.restBreakTierBucket

fun summarizePendingRestBreakAuditsForSnapshot(items: MutableList<PendingRestBreakAudit>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedValue()
    }
    return totals
}

fun deriveManualDutyPeriodSnapshotsFromAudit(items: MutableList<PendingRestBreakAudit>): MutableList<ManualDutyPeriodSnapshot> {
    val out = mutableListOf<ManualDutyPeriodSnapshot>()
    for (item in items) {
        val derived = newManualDutyPeriodSnapshot(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun resolveManualDutyPeriodSnapshot(item: ManualDutyPeriodSnapshot): String {
    return restBreakTierBucket(item.effectiveTotal())
}
