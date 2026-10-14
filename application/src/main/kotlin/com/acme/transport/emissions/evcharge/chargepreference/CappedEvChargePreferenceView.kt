package com.acme.transport.emissions.evcharge.chargepreference

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capped ev charge preference. */
data class CappedEvChargePreferenceView(
    var code: String,
    var label: String,
    var dock: String,
    var unit: String,
    var carrierText: String,
    var locale: String,
    var quantity: String,
    var statusLabel: String,
)

fun toCappedEvChargePreferenceView(item: CappedEvChargePreference): CappedEvChargePreferenceView {
    return CappedEvChargePreferenceView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 80),
        dock = item.dock,
        unit = item.unit,
        carrierText = truncate(item.carrier, 64),
        locale = item.locale,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedEvChargePreferenceViews(items: MutableList<CappedEvChargePreference>): MutableList<CappedEvChargePreferenceView> {
    val views = mutableListOf<CappedEvChargePreferenceView>()
    for (item in items) {
        views.add(toCappedEvChargePreferenceView(item))
    }
    return views
}

fun sumCappedEvChargePreferenceView(view: CappedEvChargePreferenceView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
