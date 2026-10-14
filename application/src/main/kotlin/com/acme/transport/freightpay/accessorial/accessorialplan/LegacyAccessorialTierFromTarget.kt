package com.acme.transport.freightpay.accessorial.accessorialplan

import com.acme.transport.freightpay.accessorial.policy.AccessorialTarget
import com.acme.transport.freightpay.accessorial.policy.classifyAccessorialPolicy

fun summarizeAccessorialTargetsForTier(items: MutableList<AccessorialTarget>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.netExposure()
    }
    return totals
}

fun deriveLegacyAccessorialTiersFromTarget(items: MutableList<AccessorialTarget>): MutableList<LegacyAccessorialTier> {
    val out = mutableListOf<LegacyAccessorialTier>()
    for (item in items) {
        val derived = newLegacyAccessorialTier(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun settleLegacyAccessorialTier(item: LegacyAccessorialTier): String {
    return classifyAccessorialPolicy(item.weightedTotal())
}
