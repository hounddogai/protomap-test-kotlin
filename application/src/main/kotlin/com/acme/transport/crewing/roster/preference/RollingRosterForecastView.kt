package com.acme.transport.crewing.roster.preference

/** How screens and exports show a rolling roster forecast. */
data class RollingRosterForecastView(
    var code: String,
    var title: String,
    var depotText: String,
    var quantityText: String,
    var currency: String,
    var limit: String,
    var etaMinutesText: String,
    var createdAt: String,
    var pallets: String,
    var floorValue: String,
    var locale: String,
    var statusLabel: String,
)

fun toRollingRosterForecastView(item: RollingRosterForecast): RollingRosterForecastView {
    return RollingRosterForecastView(
        code = item.code,
        title = item.title,
        depotText = item.depot,
        quantityText = item.quantity.toString(),
        currency = item.currency,
        limit = item.limit.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        createdAt = item.createdAt.toString(),
        pallets = item.pallets.toString(),
        floorValue = "%.2f".format(item.floorValue),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRosterForecastViews(items: MutableList<RollingRosterForecast>): MutableList<RollingRosterForecastView> {
    val views = mutableListOf<RollingRosterForecastView>()
    for (item in items) {
        views.add(toRollingRosterForecastView(item))
    }
    return views
}

fun sumRollingRosterForecastView(view: RollingRosterForecastView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedQuantity.toDouble() + parsedLimit.toDouble() + parsedEtaMinutes.toDouble()
}
