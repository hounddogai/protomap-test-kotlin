package com.acme.transport.lastmile.redelivery.target

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal redelivery assignment. */
data class SeasonalRedeliveryAssignmentView(
    var code: String,
    var nameText: String,
    var palletsText: String,
    var version: String,
    var fuelLiters: String,
    var weight: String,
    var segment: String,
    var active: String,
    var marginText: String,
    var archived: String,
    var carrierText: String,
    var statusLabel: String,
)

fun toSeasonalRedeliveryAssignmentView(item: SeasonalRedeliveryAssignment): SeasonalRedeliveryAssignmentView {
    return SeasonalRedeliveryAssignmentView(
        code = item.code,
        nameText = truncate(item.name, 40),
        palletsText = item.pallets.toString(),
        version = item.version.toString(),
        fuelLiters = "%.2f".format(item.fuelLiters),
        weight = "%.2f".format(item.weight),
        segment = truncate(item.segment, 64),
        active = "${item.active}",
        marginText = "%.2f".format(item.margin),
        archived = "${item.archived}",
        carrierText = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalRedeliveryAssignmentViews(items: MutableList<SeasonalRedeliveryAssignment>): MutableList<SeasonalRedeliveryAssignmentView> {
    val views = mutableListOf<SeasonalRedeliveryAssignmentView>()
    for (item in items) {
        views.add(toSeasonalRedeliveryAssignmentView(item))
    }
    return views
}

fun sumSeasonalRedeliveryAssignmentView(view: SeasonalRedeliveryAssignmentView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedVersion = view.version.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedPallets.toDouble() + parsedVersion.toDouble() + parsedFuelLiters
}
