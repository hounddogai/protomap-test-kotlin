package com.acme.transport.customs.tariff.fallbacktariffcycle

/** How screens and exports show a primary tariff exception. */
data class PrimaryTariffExceptionView(
    var code: String,
    var title: String,
    var graceDays: String,
    var discount: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toPrimaryTariffExceptionView(item: PrimaryTariffException): PrimaryTariffExceptionView {
    return PrimaryTariffExceptionView(
        code = item.code,
        title = item.title,
        graceDays = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTariffExceptionViews(items: MutableList<PrimaryTariffException>): MutableList<PrimaryTariffExceptionView> {
    val views = mutableListOf<PrimaryTariffExceptionView>()
    for (item in items) {
        views.add(toPrimaryTariffExceptionView(item))
    }
    return views
}

fun sumPrimaryTariffExceptionView(view: PrimaryTariffExceptionView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedDiscount = view.discount.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedGraceDays.toDouble() + parsedDiscount + parsedDistanceKm
}
