package com.acme.transport.linehaul.backhaul.regionalbackhaultarget

import com.acme.transport.linehaul.laneplan.planagreement.WeightedLanePlanAgreement
import com.acme.transport.linehaul.laneplan.planagreement.classifyPrimaryLanePlanDraft

fun summarizeWeightedLanePlanAgreementsForTarget(items: MutableList<WeightedLanePlanAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveWeightedBackhaulTargetsFromAgreement(items: MutableList<WeightedLanePlanAgreement>): MutableList<WeightedBackhaulTarget> {
    val out = mutableListOf<WeightedBackhaulTarget>()
    for (item in items) {
        val derived = newWeightedBackhaulTarget(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun assignWeightedBackhaulTarget(item: WeightedBackhaulTarget): String {
    return classifyPrimaryLanePlanDraft(item.measuredValue())
}
