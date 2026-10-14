package com.acme.transport.customs.tariff.tariffoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional tariff override. */
data class RegionalTariffOverrideView(
    var code: String,
    var name: String,
    var maximumText: String,
    var description: String,
    var trailerId: String,
    var source: String,
    var statusLabel: String,
)

fun toRegionalTariffOverrideView(item: RegionalTariffOverride): RegionalTariffOverrideView {
    return RegionalTariffOverrideView(
        code = truncate(item.code, 80),
        name = item.name,
        maximumText = "%.2f".format(item.maximum),
        description = truncate(item.description, 64),
        trailerId = item.trailerId,
        source = truncate(item.source, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalTariffOverrideViews(items: MutableList<RegionalTariffOverride>): MutableList<RegionalTariffOverrideView> {
    val views = mutableListOf<RegionalTariffOverrideView>()
    for (item in items) {
        views.add(toRegionalTariffOverrideView(item))
    }
    return views
}

fun sumRegionalTariffOverrideView(view: RegionalTariffOverrideView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    return parsedMaximum
}
