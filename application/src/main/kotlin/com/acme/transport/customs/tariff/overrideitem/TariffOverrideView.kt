package com.acme.transport.customs.tariff.overrideitem

/** How screens and exports show a tariff override. */
data class TariffOverrideView(
    var code: String,
    var title: String,
    var currency: String,
    var shiftCode: String,
    var odometer: String,
    var locale: String,
    var rate: String,
    var statusLabel: String,
)

fun toTariffOverrideView(item: TariffOverride): TariffOverrideView {
    return TariffOverrideView(
        code = item.code,
        title = item.title,
        currency = item.currency,
        shiftCode = item.shiftCode,
        odometer = item.odometer.toString(),
        locale = item.locale,
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffOverrideViews(items: MutableList<TariffOverride>): MutableList<TariffOverrideView> {
    val views = mutableListOf<TariffOverrideView>()
    for (item in items) {
        views.add(toTariffOverrideView(item))
    }
    return views
}

fun sumTariffOverrideView(view: TariffOverrideView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedOdometer.toDouble() + parsedRate
}
