package com.acme.transport.emissions.emissionfactor.factorpreference

import com.acme.transport.emissions.emissionfactor.rollingfactorsegment.SeasonalEmissionFactorException
import com.acme.transport.emissions.emissionfactor.rollingfactorsegment.normalizePendingEmissionFactorSegmentCode

fun summarizeSeasonalEmissionFactorExceptionsForAgreement(items: MutableList<SeasonalEmissionFactorException>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveRollingEmissionFactorAgreementsFromException(items: MutableList<SeasonalEmissionFactorException>): MutableList<RollingEmissionFactorAgreement> {
    val out = mutableListOf<RollingEmissionFactorAgreement>()
    for (item in items) {
        val derived = newRollingEmissionFactorAgreement(item.code + "-plan", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun estimateRollingEmissionFactorAgreement(item: RollingEmissionFactorAgreement): String {
    return normalizePendingEmissionFactorSegmentCode(item.code)
}
