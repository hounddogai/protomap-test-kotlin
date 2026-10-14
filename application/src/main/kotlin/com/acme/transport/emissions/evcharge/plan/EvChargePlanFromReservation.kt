package com.acme.transport.emissions.evcharge.plan

import com.acme.transport.emissions.emissionfactor.notice.EmissionFactorReservation
import com.acme.transport.emissions.emissionfactor.notice.normalizeEmissionFactorReservationCode

fun summarizeEmissionFactorReservationsForPlan(items: MutableList<EmissionFactorReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.effectiveLoad()
    }
    return totals
}

fun deriveEvChargePlansFromReservation(items: MutableList<EmissionFactorReservation>): MutableList<EvChargePlan> {
    val out = mutableListOf<EvChargePlan>()
    for (item in items) {
        val derived = newEvChargePlan(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun allocateEvChargePlan(item: EvChargePlan): String = normalizeEmissionFactorReservationCode(item.code)
