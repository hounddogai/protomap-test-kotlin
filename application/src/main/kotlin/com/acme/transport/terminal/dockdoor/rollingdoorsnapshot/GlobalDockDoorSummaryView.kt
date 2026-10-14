package com.acme.transport.terminal.dockdoor.rollingdoorsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global dock door summary. */
data class GlobalDockDoorSummaryView(
    var code: String,
    var label: String,
    var pallets: String,
    var region: String,
    var stopCount: String,
    var windowDays: String,
    var contactEmailText: String,
    var batchSize: String,
    var source: String,
    var statusLabel: String,
)

fun toGlobalDockDoorSummaryView(item: GlobalDockDoorSummary): GlobalDockDoorSummaryView {
    return GlobalDockDoorSummaryView(
        code = item.code,
        label = item.label,
        pallets = item.pallets.toString(),
        region = truncate(item.region, 40),
        stopCount = item.stopCount.toString(),
        windowDays = item.windowDays.toString(),
        contactEmailText = truncate(item.contactEmail, 64),
        batchSize = item.batchSize.toString(),
        source = item.source,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalDockDoorSummaryViews(items: MutableList<GlobalDockDoorSummary>): MutableList<GlobalDockDoorSummaryView> {
    val views = mutableListOf<GlobalDockDoorSummaryView>()
    for (item in items) {
        views.add(toGlobalDockDoorSummaryView(item))
    }
    return views
}

fun sumGlobalDockDoorSummaryView(view: GlobalDockDoorSummaryView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedWindowDays = view.windowDays.toInt()
    return parsedPallets.toDouble() + parsedStopCount.toDouble() + parsedWindowDays.toDouble()
}
