package com.acme.transport.emissions.emissionfactor.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered emission factor exception. */
data class TieredEmissionFactorExceptionView(
    var code: String,
    var title: String,
    var windowDays: String,
    var quantity: String,
    var odometer: String,
    var minimum: String,
    var country: String,
    var startsAtText: String,
    var weight: String,
    var createdAt: String,
    var ratioText: String,
    var statusLabel: String,
)

fun toTieredEmissionFactorExceptionView(item: TieredEmissionFactorException): TieredEmissionFactorExceptionView {
    return TieredEmissionFactorExceptionView(
        code = item.code,
        title = truncate(item.title, 80),
        windowDays = item.windowDays.toString(),
        quantity = item.quantity.toString(),
        odometer = item.odometer.toString(),
        minimum = "%.2f".format(item.minimum),
        country = truncate(item.country, 120),
        startsAtText = item.startsAt.toString(),
        weight = "%.2f".format(item.weight),
        createdAt = item.createdAt.toString(),
        ratioText = "%.2f".format(item.ratio),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredEmissionFactorExceptionViews(items: MutableList<TieredEmissionFactorException>): MutableList<TieredEmissionFactorExceptionView> {
    val views = mutableListOf<TieredEmissionFactorExceptionView>()
    for (item in items) {
        views.add(toTieredEmissionFactorExceptionView(item))
    }
    return views
}

fun sumTieredEmissionFactorExceptionView(view: TieredEmissionFactorExceptionView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedWindowDays.toDouble() + parsedQuantity.toDouble() + parsedOdometer.toDouble()
}
