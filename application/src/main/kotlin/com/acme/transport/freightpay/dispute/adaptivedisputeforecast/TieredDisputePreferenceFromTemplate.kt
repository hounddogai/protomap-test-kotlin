package com.acme.transport.freightpay.dispute.adaptivedisputeforecast

import com.acme.transport.freightpay.dispute.disputetemplate.LenientDisputeTemplate
import com.acme.transport.freightpay.dispute.disputetemplate.normalizeManualDisputeSnapshotCode

fun summarizeLenientDisputeTemplatesForPreference(items: MutableList<LenientDisputeTemplate>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedVolume()
    }
    return totals
}

fun deriveTieredDisputePreferencesFromTemplate(items: MutableList<LenientDisputeTemplate>): MutableList<TieredDisputePreference> {
    val out = mutableListOf<TieredDisputePreference>()
    for (item in items) {
        val derived = newTieredDisputePreference(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun rankTieredDisputePreference(item: TieredDisputePreference): String {
    return normalizeManualDisputeSnapshotCode(item.code)
}
