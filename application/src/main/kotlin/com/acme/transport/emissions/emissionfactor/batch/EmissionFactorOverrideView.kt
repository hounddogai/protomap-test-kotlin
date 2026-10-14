package com.acme.transport.emissions.emissionfactor.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a emission factor override. */
data class EmissionFactorOverrideView(
    var code: String,
    var name: String,
    var checksum: String,
    var localeText: String,
    var routeCode: String,
    var segmentText: String,
    var dock: String,
    var countryText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toEmissionFactorOverrideView(item: EmissionFactorOverride): EmissionFactorOverrideView {
    return EmissionFactorOverrideView(
        code = item.code,
        name = item.name,
        checksum = item.checksum,
        localeText = item.locale,
        routeCode = item.routeCode,
        segmentText = truncate(item.segment, 120),
        dock = truncate(item.dock, 120),
        countryText = truncate(item.country, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toEmissionFactorOverrideViews(items: MutableList<EmissionFactorOverride>): MutableList<EmissionFactorOverrideView> {
    val views = mutableListOf<EmissionFactorOverrideView>()
    for (item in items) {
        views.add(toEmissionFactorOverrideView(item))
    }
    return views
}

fun sumEmissionFactorOverrideView(view: EmissionFactorOverrideView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
