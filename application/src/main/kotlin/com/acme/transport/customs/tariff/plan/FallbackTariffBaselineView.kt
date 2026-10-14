package com.acme.transport.customs.tariff.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback tariff baseline. */
data class FallbackTariffBaselineView(
    var code: String,
    var name: String,
    var durationMinutes: String,
    var windowDays: String,
    var maximum: String,
    var statusLabel: String,
)

fun toFallbackTariffBaselineView(item: FallbackTariffBaseline): FallbackTariffBaselineView {
    return FallbackTariffBaselineView(
        code = truncate(item.code, 120),
        name = truncate(item.name, 64),
        durationMinutes = item.durationMinutes.toString(),
        windowDays = item.windowDays.toString(),
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTariffBaselineViews(items: MutableList<FallbackTariffBaseline>): MutableList<FallbackTariffBaselineView> {
    val views = mutableListOf<FallbackTariffBaselineView>()
    for (item in items) {
        views.add(toFallbackTariffBaselineView(item))
    }
    return views
}

fun sumFallbackTariffBaselineView(view: FallbackTariffBaselineView): Double {
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedDurationMinutes.toDouble() + parsedWindowDays.toDouble() + parsedMaximum
}
