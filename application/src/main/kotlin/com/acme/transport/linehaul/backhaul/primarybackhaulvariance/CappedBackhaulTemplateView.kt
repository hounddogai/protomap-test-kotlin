package com.acme.transport.linehaul.backhaul.primarybackhaulvariance

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped backhaul template. */
data class CappedBackhaulTemplateView(
    var code: String,
    var title: String,
    var reviewer: String,
    var fuelLiters: String,
    var vehicleIdText: String,
    var pinned: String,
    var regionText: String,
    var score: String,
    var discount: String,
    var expiresAt: String,
    var statusLabel: String,
)

fun toCappedBackhaulTemplateView(item: CappedBackhaulTemplate): CappedBackhaulTemplateView {
    return CappedBackhaulTemplateView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 80),
        reviewer = truncate(item.reviewer, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        vehicleIdText = item.vehicleId,
        pinned = "${item.pinned}",
        regionText = item.region,
        score = "%.2f".format(item.score),
        discount = "%.2f".format(item.discount),
        expiresAt = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedBackhaulTemplateViews(items: MutableList<CappedBackhaulTemplate>): MutableList<CappedBackhaulTemplateView> {
    val views = mutableListOf<CappedBackhaulTemplateView>()
    for (item in items) {
        views.add(toCappedBackhaulTemplateView(item))
    }
    return views
}

fun sumCappedBackhaulTemplateView(view: CappedBackhaulTemplateView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedScore = view.score.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedFuelLiters + parsedScore + parsedDiscount
}
