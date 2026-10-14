package com.acme.transport.emissions.evcharge.chargescore

import com.acme.transport.emissions.emissionfactor.schedule.EmissionFactorSchedule
import com.acme.transport.emissions.emissionfactor.schedule.emissionFactorScheduleWeight

fun summarizeEmissionFactorSchedulesForSchedule(items: MutableList<EmissionFactorSchedule>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedLoad()
    }
    return totals
}

fun deriveEvChargeSchedulesFromSchedule(items: MutableList<EmissionFactorSchedule>): MutableList<EvChargeSchedule> {
    val out = mutableListOf<EvChargeSchedule>()
    for (item in items) {
        val derived = newEvChargeSchedule(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun summarizeEvChargeSchedule(item: EvChargeSchedule): Double = emissionFactorScheduleWeight(1, true)
