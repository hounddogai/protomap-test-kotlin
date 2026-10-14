package com.acme.transport.emissions.idling.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling mapping. */
data class IdlingMappingView(
    var code: String,
    var title: String,
    var quantityText: String,
    var loadKg: String,
    var region: String,
    var lane: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toIdlingMappingView(item: IdlingMapping): IdlingMappingView {
    return IdlingMappingView(
        code = item.code,
        title = truncate(item.title, 40),
        quantityText = item.quantity.toString(),
        loadKg = "%.2f".format(item.loadKg),
        region = item.region,
        lane = item.lane,
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingMappingViews(items: MutableList<IdlingMapping>): MutableList<IdlingMappingView> {
    val views = mutableListOf<IdlingMappingView>()
    for (item in items) {
        views.add(toIdlingMappingView(item))
    }
    return views
}

fun sumIdlingMappingView(view: IdlingMappingView): Double {
    val parsedQuantity = view.quantityText.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedQuantity.toDouble() + parsedLoadKg + parsedEtaMinutes.toDouble()
}
