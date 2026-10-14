package com.acme.transport.lastmile.drop.entry

import com.acme.transport.lastmile.locker.policy.LockerAgreement
import com.acme.transport.lastmile.locker.policy.lockerAgreementBucket

fun summarizeLockerAgreementsForAudit(items: MutableList<LockerAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedValue()
    }
    return totals
}

fun deriveDropAuditsFromAgreement(items: MutableList<LockerAgreement>): MutableList<DropAudit> {
    val out = mutableListOf<DropAudit>()
    for (item in items) {
        val derived = newDropAudit(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun planDropAudit(item: DropAudit): String = lockerAgreementBucket(item.projectedLoad())
