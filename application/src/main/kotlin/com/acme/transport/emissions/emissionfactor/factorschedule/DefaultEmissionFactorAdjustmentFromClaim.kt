package com.acme.transport.emissions.emissionfactor.factorschedule

import com.acme.transport.emissions.evcharge.chargequota.StrictEvChargeClaim
import com.acme.transport.emissions.evcharge.chargequota.classifySeasonalEvChargeQuota

fun summarizeStrictEvChargeClaimsForAdjustment(items: MutableList<StrictEvChargeClaim>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveDefaultEmissionFactorAdjustmentsFromClaim(items: MutableList<StrictEvChargeClaim>): MutableList<DefaultEmissionFactorAdjustment> {
    val out = mutableListOf<DefaultEmissionFactorAdjustment>()
    for (item in items) {
        val derived = newDefaultEmissionFactorAdjustment(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun balanceDefaultEmissionFactorAdjustment(item: DefaultEmissionFactorAdjustment): String {
    return classifySeasonalEvChargeQuota(item.weightedExposure())
}
