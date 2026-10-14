package com.acme.transport.customs.declaration.declarationforecast

/** How screens and exports show a primary declaration forecast. */
data class PrimaryDeclarationForecastView(
    var code: String,
    var name: String,
    var minimum: String,
    var description: String,
    var notes: String,
    var loadKg: String,
    var rateText: String,
    var floorValueText: String,
    var ceiling: String,
    var statusLabel: String,
)

fun toPrimaryDeclarationForecastView(item: PrimaryDeclarationForecast): PrimaryDeclarationForecastView {
    return PrimaryDeclarationForecastView(
        code = item.code,
        name = item.name,
        minimum = "%.2f".format(item.minimum),
        description = item.description,
        notes = item.notes,
        loadKg = "%.2f".format(item.loadKg),
        rateText = "%.2f".format(item.rate),
        floorValueText = "%.2f".format(item.floorValue),
        ceiling = "%.2f".format(item.ceiling),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryDeclarationForecastViews(items: MutableList<PrimaryDeclarationForecast>): MutableList<PrimaryDeclarationForecastView> {
    val views = mutableListOf<PrimaryDeclarationForecastView>()
    for (item in items) {
        views.add(toPrimaryDeclarationForecastView(item))
    }
    return views
}

fun sumPrimaryDeclarationForecastView(view: PrimaryDeclarationForecastView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedMinimum + parsedLoadKg + parsedRate
}
