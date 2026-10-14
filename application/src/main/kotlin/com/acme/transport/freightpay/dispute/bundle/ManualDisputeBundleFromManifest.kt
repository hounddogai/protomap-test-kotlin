package com.acme.transport.freightpay.dispute.bundle

import com.acme.transport.freightpay.accessorial.manifest.AccessorialManifest
import com.acme.transport.freightpay.accessorial.manifest.accessorialBundleBucket

fun summarizeAccessorialManifestsForBundle(items: MutableList<AccessorialManifest>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedValue()
    }
    return totals
}

fun deriveManualDisputeBundlesFromManifest(items: MutableList<AccessorialManifest>): MutableList<ManualDisputeBundle> {
    val out = mutableListOf<ManualDisputeBundle>()
    for (item in items) {
        val derived = newManualDisputeBundle(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun allocateManualDisputeBundle(item: ManualDisputeBundle): String = accessorialBundleBucket(item.measuredValue())
