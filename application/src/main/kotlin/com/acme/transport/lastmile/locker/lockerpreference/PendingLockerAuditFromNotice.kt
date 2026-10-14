package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.lastmile.drop.weighteddropnotice.FallbackDropNotice
import com.acme.transport.lastmile.drop.weighteddropnotice.normalizeDropSummaryCode

fun summarizeFallbackDropNoticesForAudit(items: MutableList<FallbackDropNotice>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedVolume()
    }
    return totals
}

fun derivePendingLockerAuditsFromNotice(items: MutableList<FallbackDropNotice>): MutableList<PendingLockerAudit> {
    val out = mutableListOf<PendingLockerAudit>()
    for (item in items) {
        val derived = newPendingLockerAudit(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankPendingLockerAudit(item: PendingLockerAudit): String = normalizeDropSummaryCode(item.code)
