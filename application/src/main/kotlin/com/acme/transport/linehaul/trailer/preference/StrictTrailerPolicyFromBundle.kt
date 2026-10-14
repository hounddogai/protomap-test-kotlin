package com.acme.transport.linehaul.trailer.preference

import com.acme.transport.linehaul.laneplan.agreement.LanePlanBundle
import com.acme.transport.linehaul.laneplan.agreement.classifyLanePlanBundle

fun summarizeLanePlanBundlesForPolicy(items: MutableList<LanePlanBundle>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveStrictTrailerPoliciesFromBundle(items: MutableList<LanePlanBundle>): MutableList<StrictTrailerPolicy> {
    val out = mutableListOf<StrictTrailerPolicy>()
    for (item in items) {
        val derived = newStrictTrailerPolicy(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun applyStrictTrailerPolicy(item: StrictTrailerPolicy): String = classifyLanePlanBundle(item.netExposure())
