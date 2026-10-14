package com.acme.transport.lastmile.redelivery.template

import com.acme.transport.lastmile.proofofdelivery.review.CappedProofOfDeliveryProfile
import com.acme.transport.lastmile.proofofdelivery.review.proofOfDeliveryCycleBucket

fun summarizeCappedProofOfDeliveryProfilesForPreference(items: MutableList<CappedProofOfDeliveryProfile>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRedeliveryPreferencesFromProfile(items: MutableList<CappedProofOfDeliveryProfile>): MutableList<RedeliveryPreference> {
    val out = mutableListOf<RedeliveryPreference>()
    for (item in items) {
        val derived = newRedeliveryPreference(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun deriveRedeliveryPreference(item: RedeliveryPreference): String = proofOfDeliveryCycleBucket(item.netValue())
