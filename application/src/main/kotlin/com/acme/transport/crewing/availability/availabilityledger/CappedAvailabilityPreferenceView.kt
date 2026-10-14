package com.acme.transport.crewing.availability.availabilityledger

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped availability preference. */
data class CappedAvailabilityPreferenceView(
    var code: String,
    var title: String,
    var notes: String,
    var region: String,
    var source: String,
    var routeCodeText: String,
    var quantity: String,
    var statusLabel: String,
)

fun toCappedAvailabilityPreferenceView(item: CappedAvailabilityPreference): CappedAvailabilityPreferenceView {
    return CappedAvailabilityPreferenceView(
        code = item.code,
        title = item.title,
        notes = item.notes,
        region = item.region,
        source = item.source,
        routeCodeText = truncate(item.routeCode, 64),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAvailabilityPreferenceViews(items: MutableList<CappedAvailabilityPreference>): MutableList<CappedAvailabilityPreferenceView> {
    val views = mutableListOf<CappedAvailabilityPreferenceView>()
    for (item in items) {
        views.add(toCappedAvailabilityPreferenceView(item))
    }
    return views
}

fun sumCappedAvailabilityPreferenceView(view: CappedAvailabilityPreferenceView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
