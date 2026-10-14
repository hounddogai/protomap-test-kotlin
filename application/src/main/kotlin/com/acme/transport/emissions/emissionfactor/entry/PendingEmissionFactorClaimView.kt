package com.acme.transport.emissions.emissionfactor.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending emission factor claim. */
data class PendingEmissionFactorClaimView(
    var code: String,
    var label: String,
    var batchSize: String,
    var durationMinutes: String,
    var active: String,
    var visible: String,
    var etaMinutes: String,
    var stopCount: String,
    var unitText: String,
    var statusLabel: String,
)

fun toPendingEmissionFactorClaimView(item: PendingEmissionFactorClaim): PendingEmissionFactorClaimView {
    return PendingEmissionFactorClaimView(
        code = item.code,
        label = truncate(item.label, 64),
        batchSize = item.batchSize.toString(),
        durationMinutes = item.durationMinutes.toString(),
        active = "${item.active}",
        visible = "${item.visible}",
        etaMinutes = item.etaMinutes.toString(),
        stopCount = item.stopCount.toString(),
        unitText = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingEmissionFactorClaimViews(items: MutableList<PendingEmissionFactorClaim>): MutableList<PendingEmissionFactorClaimView> {
    val views = mutableListOf<PendingEmissionFactorClaimView>()
    for (item in items) {
        views.add(toPendingEmissionFactorClaimView(item))
    }
    return views
}

fun sumPendingEmissionFactorClaimView(view: PendingEmissionFactorClaimView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedBatchSize.toDouble() + parsedDurationMinutes.toDouble() + parsedEtaMinutes.toDouble()
}
