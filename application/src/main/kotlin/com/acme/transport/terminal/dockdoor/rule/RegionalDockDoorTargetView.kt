package com.acme.transport.terminal.dockdoor.rule

/** How screens and exports show a regional dock door target. */
data class RegionalDockDoorTargetView(
    var code: String,
    var title: String,
    var startsAt: String,
    var margin: String,
    var phoneText: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toRegionalDockDoorTargetView(item: RegionalDockDoorTarget): RegionalDockDoorTargetView {
    return RegionalDockDoorTargetView(
        code = item.code,
        title = item.title,
        startsAt = item.startsAt.toString(),
        margin = "%.2f".format(item.margin),
        phoneText = item.phone,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDockDoorTargetViews(items: MutableList<RegionalDockDoorTarget>): MutableList<RegionalDockDoorTargetView> {
    val views = mutableListOf<RegionalDockDoorTargetView>()
    for (item in items) {
        views.add(toRegionalDockDoorTargetView(item))
    }
    return views
}

fun sumRegionalDockDoorTargetView(view: RegionalDockDoorTargetView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedStartsAt.toDouble() + parsedMargin + parsedCreatedAt.toDouble()
}
