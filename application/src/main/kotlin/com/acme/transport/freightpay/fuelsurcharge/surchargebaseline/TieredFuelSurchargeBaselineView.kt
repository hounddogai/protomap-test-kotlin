package com.acme.transport.freightpay.fuelsurcharge.surchargebaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered fuel surcharge baseline. */
data class TieredFuelSurchargeBaselineView(
    var code: String,
    var label: String,
    var archived: String,
    var ownerIdText: String,
    var currencyText: String,
    var pinnedText: String,
    var unit: String,
    var scoreText: String,
    var carrierText: String,
    var phoneText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toTieredFuelSurchargeBaselineView(item: TieredFuelSurchargeBaseline): TieredFuelSurchargeBaselineView {
    return TieredFuelSurchargeBaselineView(
        code = truncate(item.code, 80),
        label = item.label,
        archived = "${item.archived}",
        ownerIdText = truncate(item.ownerId, 40),
        currencyText = truncate(item.currency, 40),
        pinnedText = "${item.pinned}",
        unit = item.unit,
        scoreText = "%.2f".format(item.score),
        carrierText = truncate(item.carrier, 40),
        phoneText = item.phone,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelSurchargeBaselineViews(items: MutableList<TieredFuelSurchargeBaseline>): MutableList<TieredFuelSurchargeBaselineView> {
    val views = mutableListOf<TieredFuelSurchargeBaselineView>()
    for (item in items) {
        views.add(toTieredFuelSurchargeBaselineView(item))
    }
    return views
}

fun sumTieredFuelSurchargeBaselineView(view: TieredFuelSurchargeBaselineView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedScore + parsedDistanceKm
}
