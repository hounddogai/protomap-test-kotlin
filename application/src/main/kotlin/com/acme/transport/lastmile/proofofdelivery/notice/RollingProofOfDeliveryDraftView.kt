package com.acme.transport.lastmile.proofofdelivery.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling proof of delivery draft. */
data class RollingProofOfDeliveryDraftView(
    var code: String,
    var name: String,
    var quantityText: String,
    var batchSize: String,
    var odometer: String,
    var pallets: String,
    var updatedAt: String,
    var categoryText: String,
    var statusLabel: String,
)

fun toRollingProofOfDeliveryDraftView(item: RollingProofOfDeliveryDraft): RollingProofOfDeliveryDraftView {
    return RollingProofOfDeliveryDraftView(
        code = item.code,
        name = truncate(item.name, 40),
        quantityText = item.quantity.toString(),
        batchSize = item.batchSize.toString(),
        odometer = item.odometer.toString(),
        pallets = item.pallets.toString(),
        updatedAt = item.updatedAt.toString(),
        categoryText = item.category,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingProofOfDeliveryDraftViews(items: MutableList<RollingProofOfDeliveryDraft>): MutableList<RollingProofOfDeliveryDraftView> {
    val views = mutableListOf<RollingProofOfDeliveryDraftView>()
    for (item in items) {
        views.add(toRollingProofOfDeliveryDraftView(item))
    }
    return views
}

fun sumRollingProofOfDeliveryDraftView(view: RollingProofOfDeliveryDraftView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedQuantity.toDouble() + parsedBatchSize.toDouble() + parsedOdometer.toDouble()
}
