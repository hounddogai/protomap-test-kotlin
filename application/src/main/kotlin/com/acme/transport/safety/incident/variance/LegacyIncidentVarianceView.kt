package com.acme.transport.safety.incident.variance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy incident variance. */
data class LegacyIncidentVarianceView(
    var codeText: String,
    var label: String,
    var quantity: String,
    var pallets: String,
    var titleText: String,
    var vehicleId: String,
    var visible: String,
    var contactEmail: String,
    var scoreText: String,
    var statusLabel: String,
)

fun toLegacyIncidentVarianceView(item: LegacyIncidentVariance): LegacyIncidentVarianceView {
    return LegacyIncidentVarianceView(
        codeText = truncate(item.code, 120),
        label = item.label,
        quantity = item.quantity.toString(),
        pallets = item.pallets.toString(),
        titleText = item.title,
        vehicleId = item.vehicleId,
        visible = "${item.visible}",
        contactEmail = item.contactEmail,
        scoreText = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyIncidentVarianceViews(items: MutableList<LegacyIncidentVariance>): MutableList<LegacyIncidentVarianceView> {
    val views = mutableListOf<LegacyIncidentVarianceView>()
    for (item in items) {
        views.add(toLegacyIncidentVarianceView(item))
    }
    return views
}

fun sumLegacyIncidentVarianceView(view: LegacyIncidentVarianceView): Double {
    val parsedQuantity = view.quantity.toInt()
    val parsedPallets = view.pallets.toInt()
    val parsedScore = view.scoreText.toDouble()
    return parsedQuantity.toDouble() + parsedPallets.toDouble() + parsedScore
}
