package com.acme.transport.customs.tariff.agreement

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff segment. */
data class TariffSegmentView(
    var code: String,
    var title: String,
    var unit: String,
    var maximum: String,
    var distanceKm: String,
    var source: String,
    var statusLabel: String,
)

fun toTariffSegmentView(item: TariffSegment): TariffSegmentView {
    return TariffSegmentView(
        code = truncate(item.code, 40),
        title = item.title,
        unit = item.unit,
        maximum = "%.2f".format(item.maximum),
        distanceKm = "%.2f".format(item.distanceKm),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffSegmentViews(items: MutableList<TariffSegment>): MutableList<TariffSegmentView> {
    val views = mutableListOf<TariffSegmentView>()
    for (item in items) {
        views.add(toTariffSegmentView(item))
    }
    return views
}

fun sumTariffSegmentView(view: TariffSegmentView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedMaximum + parsedDistanceKm
}
