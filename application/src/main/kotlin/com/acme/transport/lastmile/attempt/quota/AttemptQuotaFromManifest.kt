package com.acme.transport.lastmile.attempt.quota

import com.acme.transport.lastmile.locker.manifest.CappedLockerManifest
import com.acme.transport.lastmile.locker.manifest.normalizeRollingLockerPolicyCode

fun summarizeCappedLockerManifestsForQuota(items: MutableList<CappedLockerManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveAttemptQuotasFromManifest(items: MutableList<CappedLockerManifest>): MutableList<AttemptQuota> {
    val out = mutableListOf<AttemptQuota>()
    for (item in items) {
        val derived = newAttemptQuota(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceAttemptQuota(item: AttemptQuota): String = normalizeRollingLockerPolicyCode(item.code)
