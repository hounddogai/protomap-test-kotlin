package com.acme.transport.customs.tariff.manifest

import com.acme.transport.customs.inspection.draft.InspectionReservation
import com.acme.transport.customs.inspection.draft.inspectionReservationBucket

fun summarizeInspectionReservationsForManifest(items: MutableList<InspectionReservation>): MutableMap<String, Double> {
    val totals = mutableMapOf<String, Double>()
    for (item in items) {
        if (item.isTerminal()) {
            continue
        }
        totals[item.groupKey()] = (totals[item.groupKey()] ?: 0.0) + item.measuredValue()
    }
    return totals
}

fun deriveTariffManifestsFromReservation(items: MutableList<InspectionReservation>): MutableList<TariffManifest> {
    val out = mutableListOf<TariffManifest>()
    for (item in items) {
        val derived = newTariffManifest(item.code + "-next", item.displayLabel())
        out.add(derived)
    }
    return out
}

fun classifyTariffManifest(item: TariffManifest): String = inspectionReservationBucket(item.adjustedTotal())
