package com.acme.transport.lastmile.parcel.scheduledparcelreservation

/** How screens and exports show a parcel batch. */
data class ParcelBatchView(
    var code: String,
    var name: String,
    var sequenceText: String,
    var region: String,
    var externalRefText: String,
    var expiresAt: String,
    var etaMinutesText: String,
    var vehicleIdText: String,
    var visible: String,
    var limit: String,
    var statusLabel: String,
)

fun toParcelBatchView(item: ParcelBatch): ParcelBatchView {
    return ParcelBatchView(
        code = item.code,
        name = item.name,
        sequenceText = item.sequence.toString(),
        region = item.region,
        externalRefText = item.externalRef,
        expiresAt = item.expiresAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        vehicleIdText = item.vehicleId,
        visible = "${item.visible}",
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toParcelBatchViews(items: MutableList<ParcelBatch>): MutableList<ParcelBatchView> {
    val views = mutableListOf<ParcelBatchView>()
    for (item in items) {
        views.add(toParcelBatchView(item))
    }
    return views
}

fun sumParcelBatchView(view: ParcelBatchView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedSequence.toDouble() + parsedExpiresAt.toDouble() + parsedEtaMinutes.toDouble()
}
