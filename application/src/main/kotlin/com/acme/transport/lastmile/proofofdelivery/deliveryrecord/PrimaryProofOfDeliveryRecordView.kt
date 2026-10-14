package com.acme.transport.lastmile.proofofdelivery.deliveryrecord

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary proof of delivery record. */
data class PrimaryProofOfDeliveryRecordView(
    var code: String,
    var titleText: String,
    var phone: String,
    var palletsText: String,
    var revision: String,
    var source: String,
    var vehicleId: String,
    var unit: String,
    var score: String,
    var loadKg: String,
    var depot: String,
    var statusLabel: String,
)

fun toPrimaryProofOfDeliveryRecordView(item: PrimaryProofOfDeliveryRecord): PrimaryProofOfDeliveryRecordView {
    return PrimaryProofOfDeliveryRecordView(
        code = truncate(item.code, 120),
        titleText = truncate(item.title, 120),
        phone = item.phone,
        palletsText = item.pallets.toString(),
        revision = item.revision.toString(),
        source = item.source,
        vehicleId = truncate(item.vehicleId, 64),
        unit = truncate(item.unit, 64),
        score = "%.2f".format(item.score),
        loadKg = "%.2f".format(item.loadKg),
        depot = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryProofOfDeliveryRecordViews(items: MutableList<PrimaryProofOfDeliveryRecord>): MutableList<PrimaryProofOfDeliveryRecordView> {
    val views = mutableListOf<PrimaryProofOfDeliveryRecordView>()
    for (item in items) {
        views.add(toPrimaryProofOfDeliveryRecordView(item))
    }
    return views
}

fun sumPrimaryProofOfDeliveryRecordView(view: PrimaryProofOfDeliveryRecordView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedScore = view.score.toDouble()
    return parsedPallets.toDouble() + parsedRevision.toDouble() + parsedScore
}
