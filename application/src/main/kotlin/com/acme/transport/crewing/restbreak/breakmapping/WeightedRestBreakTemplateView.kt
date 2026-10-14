package com.acme.transport.crewing.restbreak.breakmapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted rest break template. */
data class WeightedRestBreakTemplateView(
    var code: String,
    var labelText: String,
    var channel: String,
    var expiresAt: String,
    var odometer: String,
    var pallets: String,
    var fuelLiters: String,
    var minimum: String,
    var statusLabel: String,
)

fun toWeightedRestBreakTemplateView(item: WeightedRestBreakTemplate): WeightedRestBreakTemplateView {
    return WeightedRestBreakTemplateView(
        code = item.code,
        labelText = item.label,
        channel = truncate(item.channel, 80),
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        pallets = item.pallets.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRestBreakTemplateViews(items: MutableList<WeightedRestBreakTemplate>): MutableList<WeightedRestBreakTemplateView> {
    val views = mutableListOf<WeightedRestBreakTemplateView>()
    for (item in items) {
        views.add(toWeightedRestBreakTemplateView(item))
    }
    return views
}

fun sumWeightedRestBreakTemplateView(view: WeightedRestBreakTemplateView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedPallets = view.pallets.toInt()
    return parsedExpiresAt.toDouble() + parsedOdometer.toDouble() + parsedPallets.toDouble()
}
