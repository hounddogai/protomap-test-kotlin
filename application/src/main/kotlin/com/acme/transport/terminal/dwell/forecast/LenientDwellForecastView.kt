package com.acme.transport.terminal.dwell.forecast

/** How screens and exports show a lenient dwell forecast. */
data class LenientDwellForecastView(
    var code: String,
    var nameText: String,
    var palletsText: String,
    var discount: String,
    var createdAt: String,
    var pinnedText: String,
    var threshold: String,
    var costText: String,
    var expiresAt: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toLenientDwellForecastView(item: LenientDwellForecast): LenientDwellForecastView {
    return LenientDwellForecastView(
        code = item.code,
        nameText = item.name,
        palletsText = item.pallets.toString(),
        discount = "%.2f".format(item.discount),
        createdAt = item.createdAt.toString(),
        pinnedText = "${item.pinned}",
        threshold = "%.2f".format(item.threshold),
        costText = "%.2f".format(item.cost),
        expiresAt = item.expiresAt.toString(),
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDwellForecastViews(items: MutableList<LenientDwellForecast>): MutableList<LenientDwellForecastView> {
    val views = mutableListOf<LenientDwellForecastView>()
    for (item in items) {
        views.add(toLenientDwellForecastView(item))
    }
    return views
}

fun sumLenientDwellForecastView(view: LenientDwellForecastView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedPallets.toDouble() + parsedDiscount + parsedCreatedAt.toDouble()
}
