package com.acme.transport.lastmile.proofofdelivery.deliveryallocation

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default proof of delivery manifest. */
data class DefaultProofOfDeliveryManifestView(
    var codeText: String,
    var labelText: String,
    var priority: String,
    var quantityText: String,
    var weight: String,
    var shiftCode: String,
    var costText: String,
    var graceDays: String,
    var summary: String,
    var unit: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toDefaultProofOfDeliveryManifestView(item: DefaultProofOfDeliveryManifest): DefaultProofOfDeliveryManifestView {
    return DefaultProofOfDeliveryManifestView(
        codeText = item.code,
        labelText = truncate(item.label, 40),
        priority = item.priority.toString(),
        quantityText = item.quantity.toString(),
        weight = "%.2f".format(item.weight),
        shiftCode = item.shiftCode,
        costText = "%.2f".format(item.cost),
        graceDays = item.graceDays.toString(),
        summary = item.summary,
        unit = item.unit,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultProofOfDeliveryManifestViews(items: MutableList<DefaultProofOfDeliveryManifest>): MutableList<DefaultProofOfDeliveryManifestView> {
    val views = mutableListOf<DefaultProofOfDeliveryManifestView>()
    for (item in items) {
        views.add(toDefaultProofOfDeliveryManifestView(item))
    }
    return views
}

fun sumDefaultProofOfDeliveryManifestView(view: DefaultProofOfDeliveryManifestView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedQuantity = view.quantityText.toInt()
    val parsedWeight = view.weight.toDouble()
    return parsedPriority.toDouble() + parsedQuantity.toDouble() + parsedWeight
}
