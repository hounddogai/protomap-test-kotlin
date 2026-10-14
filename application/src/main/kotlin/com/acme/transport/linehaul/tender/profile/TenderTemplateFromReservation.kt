package com.acme.transport.linehaul.tender.profile

import com.acme.transport.linehaul.laneplan.reservation.LanePlanReservation
import com.acme.transport.linehaul.laneplan.reservation.lanePlanTargetWeight

fun summarizeLanePlanReservationsForTemplate(items: MutableList<LanePlanReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.weightedExposure()
    }
    return totals
}

fun deriveTenderTemplatesFromReservation(items: MutableList<LanePlanReservation>): MutableList<TenderTemplate> {
    val out = mutableListOf<TenderTemplate>()
    for (item in items) {
        val derived = newTenderTemplate(item.code + "-plan", item.label())
        out.add(derived)
    }
    return out
}

fun balanceTenderTemplate(item: TenderTemplate): Double = lanePlanTargetWeight(6, true)
