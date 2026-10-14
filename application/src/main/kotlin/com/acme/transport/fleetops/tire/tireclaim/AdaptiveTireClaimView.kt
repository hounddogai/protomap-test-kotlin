package com.acme.transport.fleetops.tire.tireclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tire claim. */
data class AdaptiveTireClaimView(
    var codeText: String,
    var titleText: String,
    var dockText: String,
    var currency: String,
    var fuelLitersText: String,
    var stopCount: String,
    var contactEmailText: String,
    var statusLabel: String,
)

fun toAdaptiveTireClaimView(item: AdaptiveTireClaim): AdaptiveTireClaimView {
    return AdaptiveTireClaimView(
        codeText = item.code,
        titleText = item.title,
        dockText = item.dock,
        currency = item.currency,
        fuelLitersText = "%.2f".format(item.fuelLiters),
        stopCount = item.stopCount.toString(),
        contactEmailText = truncate(item.contactEmail, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTireClaimViews(items: MutableList<AdaptiveTireClaim>): MutableList<AdaptiveTireClaimView> {
    val views = mutableListOf<AdaptiveTireClaimView>()
    for (item in items) {
        views.add(toAdaptiveTireClaimView(item))
    }
    return views
}

fun sumAdaptiveTireClaimView(view: AdaptiveTireClaimView): Double {
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    return parsedFuelLiters + parsedStopCount.toDouble()
}
