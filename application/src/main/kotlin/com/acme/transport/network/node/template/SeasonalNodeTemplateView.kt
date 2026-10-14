package com.acme.transport.network.node.template

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal node template. */
data class SeasonalNodeTemplateView(
    var codeText: String,
    var labelText: String,
    var odometer: String,
    var palletsText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toSeasonalNodeTemplateView(item: SeasonalNodeTemplate): SeasonalNodeTemplateView {
    return SeasonalNodeTemplateView(
        codeText = item.code,
        labelText = item.label,
        odometer = item.odometer.toString(),
        palletsText = item.pallets.toString(),
        carrier = truncate(item.carrier, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalNodeTemplateViews(items: MutableList<SeasonalNodeTemplate>): MutableList<SeasonalNodeTemplateView> {
    val views = mutableListOf<SeasonalNodeTemplateView>()
    for (item in items) {
        views.add(toSeasonalNodeTemplateView(item))
    }
    return views
}

fun sumSeasonalNodeTemplateView(view: SeasonalNodeTemplateView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedPallets = view.palletsText.toInt()
    return parsedOdometer.toDouble() + parsedPallets.toDouble()
}
