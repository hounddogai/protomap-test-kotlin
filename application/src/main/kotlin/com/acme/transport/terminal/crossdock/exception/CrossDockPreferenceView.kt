package com.acme.transport.terminal.crossdock.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a cross dock preference. */
data class CrossDockPreferenceView(
    var codeText: String,
    var title: String,
    var maximum: String,
    var fuelLitersText: String,
    var minimum: String,
    var descriptionText: String,
    var externalRefText: String,
    var segment: String,
    var statusLabel: String,
)

fun toCrossDockPreferenceView(item: CrossDockPreference): CrossDockPreferenceView {
    return CrossDockPreferenceView(
        codeText = truncate(item.code, 64),
        title = item.title,
        maximum = "%.2f".format(item.maximum),
        fuelLitersText = "%.2f".format(item.fuelLiters),
        minimum = "%.2f".format(item.minimum),
        descriptionText = truncate(item.description, 64),
        externalRefText = truncate(item.externalRef, 64),
        segment = item.segment,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCrossDockPreferenceViews(items: MutableList<CrossDockPreference>): MutableList<CrossDockPreferenceView> {
    val views = mutableListOf<CrossDockPreferenceView>()
    for (item in items) {
        views.add(toCrossDockPreferenceView(item))
    }
    return views
}

fun sumCrossDockPreferenceView(view: CrossDockPreferenceView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedFuelLiters = view.fuelLitersText.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedMaximum + parsedFuelLiters + parsedMinimum
}
