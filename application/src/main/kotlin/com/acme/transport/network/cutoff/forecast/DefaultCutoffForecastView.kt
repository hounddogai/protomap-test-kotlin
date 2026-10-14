package com.acme.transport.network.cutoff.forecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default cutoff forecast. */
data class DefaultCutoffForecastView(
    var code: String,
    var labelText: String,
    var phone: String,
    var title: String,
    var category: String,
    var shiftCode: String,
    var contactEmail: String,
    var quantity: String,
    var statusLabel: String,
)

fun toDefaultCutoffForecastView(item: DefaultCutoffForecast): DefaultCutoffForecastView {
    return DefaultCutoffForecastView(
        code = item.code,
        labelText = truncate(item.label, 40),
        phone = item.phone,
        title = truncate(item.title, 120),
        category = item.category,
        shiftCode = truncate(item.shiftCode, 80),
        contactEmail = item.contactEmail,
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultCutoffForecastViews(items: MutableList<DefaultCutoffForecast>): MutableList<DefaultCutoffForecastView> {
    val views = mutableListOf<DefaultCutoffForecastView>()
    for (item in items) {
        views.add(toDefaultCutoffForecastView(item))
    }
    return views
}

fun sumDefaultCutoffForecastView(view: DefaultCutoffForecastView): Double {
    val parsedQuantity = view.quantity.toInt()
    return parsedQuantity.toDouble()
}
