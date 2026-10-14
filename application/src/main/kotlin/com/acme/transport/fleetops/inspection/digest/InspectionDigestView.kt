package com.acme.transport.fleetops.inspection.digest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a inspection digest. */
data class InspectionDigestView(
    var code: String,
    var label: String,
    var unit: String,
    var externalRef: String,
    var etaMinutesText: String,
    var score: String,
    var ownerIdText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toInspectionDigestView(item: InspectionDigest): InspectionDigestView {
    return InspectionDigestView(
        code = item.code,
        label = item.label,
        unit = item.unit,
        externalRef = truncate(item.externalRef, 80),
        etaMinutesText = item.etaMinutes.toString(),
        score = "%.2f".format(item.score),
        ownerIdText = truncate(item.ownerId, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toInspectionDigestViews(items: MutableList<InspectionDigest>): MutableList<InspectionDigestView> {
    val views = mutableListOf<InspectionDigestView>()
    for (item in items) {
        views.add(toInspectionDigestView(item))
    }
    return views
}

fun sumInspectionDigestView(view: InspectionDigestView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedScore = view.score.toDouble()
    val parsedQuantity = view.quantity.toInt()
    return parsedEtaMinutes.toDouble() + parsedScore + parsedQuantity.toDouble()
}
