package com.acme.transport.lastmile.redelivery.manifest

import com.acme.transport.lastmile.locker.lockerclaim.FallbackLockerDraft
import com.acme.transport.lastmile.locker.lockerclaim.primaryLockerDraftBucket

fun summarizeFallbackLockerDraftsForManifest(items: MutableList<FallbackLockerDraft>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun derivePrimaryRedeliveryManifestsFromDraft(items: MutableList<FallbackLockerDraft>): MutableList<PrimaryRedeliveryManifest> {
    val out = mutableListOf<PrimaryRedeliveryManifest>()
    for (item in items) {
        val derived = newPrimaryRedeliveryManifest(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun classifyPrimaryRedeliveryManifest(item: PrimaryRedeliveryManifest): String {
    return primaryLockerDraftBucket(item.weightedValue())
}
