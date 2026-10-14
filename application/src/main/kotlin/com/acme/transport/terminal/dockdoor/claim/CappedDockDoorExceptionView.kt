package com.acme.transport.terminal.dockdoor.claim

/** How screens and exports show a capped dock door exception. */
data class CappedDockDoorExceptionView(
    var codeText: String,
    var titleText: String,
    var floorValue: String,
    var distanceKmText: String,
    var approved: String,
    var ceilingText: String,
    var externalRef: String,
    var dockText: String,
    var statusLabel: String,
)

fun toCappedDockDoorExceptionView(item: CappedDockDoorException): CappedDockDoorExceptionView {
    return CappedDockDoorExceptionView(
        codeText = item.code,
        titleText = item.title,
        floorValue = "%.2f".format(item.floorValue),
        distanceKmText = "%.2f".format(item.distanceKm),
        approved = "${item.approved}",
        ceilingText = "%.2f".format(item.ceiling),
        externalRef = item.externalRef,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedDockDoorExceptionViews(items: MutableList<CappedDockDoorException>): MutableList<CappedDockDoorExceptionView> {
    val views = mutableListOf<CappedDockDoorExceptionView>()
    for (item in items) {
        views.add(toCappedDockDoorExceptionView(item))
    }
    return views
}

fun sumCappedDockDoorExceptionView(view: CappedDockDoorExceptionView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedCeiling = view.ceilingText.toDouble()
    return parsedFloorValue + parsedDistanceKm + parsedCeiling
}
