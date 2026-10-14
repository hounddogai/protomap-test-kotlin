package com.acme.transport.customs.tariff.variance

/** How screens and exports show a manual tariff variance. */
data class ManualTariffVarianceView(
    var code: String,
    var name: String,
    var version: String,
    var active: String,
    var statusLabel: String,
)

fun toManualTariffVarianceView(item: ManualTariffVariance): ManualTariffVarianceView {
    return ManualTariffVarianceView(
        code = item.code,
        name = item.name,
        version = item.version.toString(),
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTariffVarianceViews(items: MutableList<ManualTariffVariance>): MutableList<ManualTariffVarianceView> {
    val views = mutableListOf<ManualTariffVarianceView>()
    for (item in items) {
        views.add(toManualTariffVarianceView(item))
    }
    return views
}

fun sumManualTariffVarianceView(view: ManualTariffVarianceView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
