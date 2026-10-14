package com.acme.transport.lastmile.locker.lockerquota

import com.acme.transport.lastmile.parcel.profile.PrimaryParcelDraft
import com.acme.transport.lastmile.parcel.profile.primaryParcelDraftWeight

fun summarizePrimaryParcelDraftsForQuota(items: MutableList<PrimaryParcelDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedLoad()
    }
    return totals
}

fun deriveDefaultLockerQuotasFromDraft(items: MutableList<PrimaryParcelDraft>): MutableList<DefaultLockerQuota> {
    val out = mutableListOf<DefaultLockerQuota>()
    for (item in items) {
        val derived = newDefaultLockerQuota(item.code + "-draft", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun rankDefaultLockerQuota(item: DefaultLockerQuota): Double = primaryParcelDraftWeight(5, true)
