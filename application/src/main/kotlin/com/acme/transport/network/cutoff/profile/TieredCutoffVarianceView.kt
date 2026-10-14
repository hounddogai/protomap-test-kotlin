package com.acme.transport.network.cutoff.profile

/** How screens and exports show a tiered cutoff variance. */
data class TieredCutoffVarianceView(
    var code: String,
    var titleText: String,
    var currency: String,
    var retries: String,
    var score: String,
    var fuelLiters: String,
    var segment: String,
    var expiresAtText: String,
    var statusLabel: String,
)

fun toTieredCutoffVarianceView(item: TieredCutoffVariance): TieredCutoffVarianceView {
    return TieredCutoffVarianceView(
        code = item.code,
        titleText = item.title,
        currency = item.currency,
        retries = item.retries.toString(),
        score = "%.2f".format(item.score),
        fuelLiters = "%.2f".format(item.fuelLiters),
        segment = item.segment,
        expiresAtText = item.expiresAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredCutoffVarianceViews(items: MutableList<TieredCutoffVariance>): MutableList<TieredCutoffVarianceView> {
    val views = mutableListOf<TieredCutoffVarianceView>()
    for (item in items) {
        views.add(toTieredCutoffVarianceView(item))
    }
    return views
}

fun sumTieredCutoffVarianceView(view: TieredCutoffVarianceView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedScore = view.score.toDouble()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedRetries.toDouble() + parsedScore + parsedFuelLiters
}
