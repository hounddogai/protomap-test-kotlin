package com.acme.transport.lastmile.locker.lockerpreference

import com.acme.transport.lastmile.redelivery.redeliveryclaim.PrimaryRedeliveryClaim
import com.acme.transport.lastmile.redelivery.redeliveryclaim.normalizeRedeliveryRuleCode

fun summarizePrimaryRedeliveryClaimsForAudit(items: MutableList<PrimaryRedeliveryClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedVolume()
    }
    return totals
}

fun derivePendingLockerAuditsFromClaim(items: MutableList<PrimaryRedeliveryClaim>): MutableList<PendingLockerAudit> {
    val out = mutableListOf<PendingLockerAudit>()
    for (item in items) {
        val derived = newPendingLockerAudit(item.code + "-copy", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settlePendingLockerAudit(item: PendingLockerAudit): String = normalizeRedeliveryRuleCode(item.code)
