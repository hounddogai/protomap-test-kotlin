package com.acme.transport.crewing.qualification.reservation

import com.acme.transport.crewing.dutyperiod.ledger.DutyPeriodLedger
import com.acme.transport.crewing.dutyperiod.ledger.dutyPeriodLedgerBucket

fun summarizeDutyPeriodLedgersForReservation(items: MutableList<DutyPeriodLedger>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveCappedQualificationReservationsFromLedger(items: MutableList<DutyPeriodLedger>): MutableList<CappedQualificationReservation> {
    val out = mutableListOf<CappedQualificationReservation>()
    for (item in items) {
        val derived = newCappedQualificationReservation(item.code + "-next", item.label())
        out.add(derived)
    }
    return out
}

fun computeCappedQualificationReservation(item: CappedQualificationReservation): String {
    return dutyPeriodLedgerBucket(item.effectiveValue())
}
