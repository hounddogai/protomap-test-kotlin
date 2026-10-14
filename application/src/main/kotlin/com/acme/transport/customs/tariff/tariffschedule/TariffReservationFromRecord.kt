package com.acme.transport.customs.tariff.tariffschedule

import com.acme.transport.customs.declaration.rule.FallbackDeclarationRecord
import com.acme.transport.customs.declaration.rule.normalizeFallbackDeclarationRecordCode

fun summarizeFallbackDeclarationRecordsForReservation(items: MutableList<FallbackDeclarationRecord>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTariffReservationsFromRecord(items: MutableList<FallbackDeclarationRecord>): MutableList<TariffReservation> {
    val out = mutableListOf<TariffReservation>()
    for (item in items) {
        val derived = newTariffReservation(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun mergeTariffReservationExtra(item: TariffReservation): String {
    return normalizeFallbackDeclarationRecordCode(item.code)
}
