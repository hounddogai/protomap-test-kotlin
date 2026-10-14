package com.acme.transport.network.transittime.forecast

/** How screens and exports show a primary transit time bundle. */
data class PrimaryTransitTimeBundleView(
    var code: String,
    var nameText: String,
    var floorValueText: String,
    var routeCode: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toPrimaryTransitTimeBundleView(item: PrimaryTransitTimeBundle): PrimaryTransitTimeBundleView {
    return PrimaryTransitTimeBundleView(
        code = item.code,
        nameText = item.name,
        floorValueText = "%.2f".format(item.floorValue),
        routeCode = item.routeCode,
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryTransitTimeBundleViews(items: MutableList<PrimaryTransitTimeBundle>): MutableList<PrimaryTransitTimeBundleView> {
    val views = mutableListOf<PrimaryTransitTimeBundleView>()
    for (item in items) {
        views.add(toPrimaryTransitTimeBundleView(item))
    }
    return views
}

fun sumPrimaryTransitTimeBundleView(view: PrimaryTransitTimeBundleView): Double {
    val parsedFloorValue = view.floorValueText.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFloorValue + parsedDurationMinutes.toDouble()
}
