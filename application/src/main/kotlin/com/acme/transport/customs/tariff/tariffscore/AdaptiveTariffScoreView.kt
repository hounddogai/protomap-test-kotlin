package com.acme.transport.customs.tariff.tariffscore

/** How screens and exports show a adaptive tariff score. */
data class AdaptiveTariffScoreView(
    var code: String,
    var label: String,
    var externalRef: String,
    var shiftCode: String,
    var ceiling: String,
    var currency: String,
    var active: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toAdaptiveTariffScoreView(item: AdaptiveTariffScore): AdaptiveTariffScoreView {
    return AdaptiveTariffScoreView(
        code = item.code,
        label = item.label,
        externalRef = item.externalRef,
        shiftCode = item.shiftCode,
        ceiling = "%.2f".format(item.ceiling),
        currency = item.currency,
        active = "${item.active}",
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTariffScoreViews(items: MutableList<AdaptiveTariffScore>): MutableList<AdaptiveTariffScoreView> {
    val views = mutableListOf<AdaptiveTariffScoreView>()
    for (item in items) {
        views.add(toAdaptiveTariffScoreView(item))
    }
    return views
}

fun sumAdaptiveTariffScoreView(view: AdaptiveTariffScoreView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedCeiling + parsedGraceDays.toDouble()
}
