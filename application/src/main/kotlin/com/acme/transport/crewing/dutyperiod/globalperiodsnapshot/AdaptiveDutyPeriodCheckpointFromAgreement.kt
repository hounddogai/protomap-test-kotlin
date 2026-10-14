package com.acme.transport.crewing.dutyperiod.globalperiodsnapshot

import com.acme.transport.crewing.swap.agreement.RollingSwapAgreement
import com.acme.transport.crewing.swap.agreement.normalizeRollingSwapAgreementCode

fun summarizeRollingSwapAgreementsForCheckpoint(items: MutableList<RollingSwapAgreement>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.adjustedTotal()
    }
    return totals
}

fun deriveAdaptiveDutyPeriodCheckpointsFromAgreement(items: MutableList<RollingSwapAgreement>): MutableList<AdaptiveDutyPeriodCheckpoint> {
    val out = mutableListOf<AdaptiveDutyPeriodCheckpoint>()
    for (item in items) {
        val derived = newAdaptiveDutyPeriodCheckpoint(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun deriveAdaptiveDutyPeriodCheckpoint(item: AdaptiveDutyPeriodCheckpoint): String {
    return normalizeRollingSwapAgreementCode(item.code)
}
