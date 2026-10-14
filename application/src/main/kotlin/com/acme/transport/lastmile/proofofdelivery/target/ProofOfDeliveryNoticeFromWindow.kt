package com.acme.transport.lastmile.proofofdelivery.target

import com.acme.transport.lastmile.redelivery.window.PendingRedeliveryWindow
import com.acme.transport.lastmile.redelivery.window.normalizeDefaultRedeliveryBundleCode

fun summarizePendingRedeliveryWindowsForNotice(items: MutableList<PendingRedeliveryWindow>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveProofOfDeliveryNoticesFromWindow(items: MutableList<PendingRedeliveryWindow>): MutableList<ProofOfDeliveryNotice> {
    val out = mutableListOf<ProofOfDeliveryNotice>()
    for (item in items) {
        val derived = newProofOfDeliveryNotice(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun computeProofOfDeliveryNotice(item: ProofOfDeliveryNotice): String {
    return normalizeDefaultRedeliveryBundleCode(item.code)
}
