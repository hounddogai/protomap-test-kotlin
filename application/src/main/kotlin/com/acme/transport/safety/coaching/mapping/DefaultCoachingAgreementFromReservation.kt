package com.acme.transport.safety.coaching.mapping

import com.acme.transport.safety.coaching.exception.CappedCoachingReservation
import com.acme.transport.safety.coaching.exception.normalizeCappedCoachingReservationCode

fun summarizeCappedCoachingReservationsForAgreement(items: MutableList<CappedCoachingReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.projectedLoad()
    }
    return totals
}

fun deriveDefaultCoachingAgreementsFromReservation(items: MutableList<CappedCoachingReservation>): MutableList<DefaultCoachingAgreement> {
    val out = mutableListOf<DefaultCoachingAgreement>()
    for (item in items) {
        val derived = newDefaultCoachingAgreement(item.code + "-copy", item.label())
        out.add(derived)
    }
    return out
}

fun estimateDefaultCoachingAgreement(item: DefaultCoachingAgreement): String {
    return normalizeCappedCoachingReservationCode(item.code)
}
