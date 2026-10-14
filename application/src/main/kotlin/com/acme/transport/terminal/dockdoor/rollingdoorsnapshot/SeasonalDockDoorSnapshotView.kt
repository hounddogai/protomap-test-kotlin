package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal dock door snapshot. */
data class SeasonalDockDoorSnapshotView(
    var codeText: String,
    var titleText: String,
    var phone: String,
    var fuelLiters: String,
    var archivedText: String,
    var statusLabel: String,
)

fun toSeasonalDockDoorSnapshotView(item: SeasonalDockDoorSnapshot): SeasonalDockDoorSnapshotView {
    return SeasonalDockDoorSnapshotView(
        codeText = item.code,
        titleText = truncate(item.title, 120),
        phone = truncate(item.phone, 120),
        fuelLiters = "%.2f".format(item.fuelLiters),
        archivedText = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDockDoorSnapshotViews(items: MutableList<SeasonalDockDoorSnapshot>): MutableList<SeasonalDockDoorSnapshotView> {
    val views = mutableListOf<SeasonalDockDoorSnapshotView>()
    for (item in items) {
        views.add(toSeasonalDockDoorSnapshotView(item))
    }
    return views
}

fun sumSeasonalDockDoorSnapshotView(view: SeasonalDockDoorSnapshotView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
