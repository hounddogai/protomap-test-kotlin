package com.acme.transport.lastmile.locker.forecast

/** How screens and exports show a locker forecast. */
data class LockerForecastView(
    var code: String,
    var label: String,
    var checksum: String,
    var dock: String,
    var floorValue: String,
    var priority: String,
    var pinned: String,
    var durationMinutes: String,
    var statusLabel: String,
)

fun toLockerForecastView(item: LockerForecast): LockerForecastView {
    return LockerForecastView(
        code = item.code,
        label = item.label,
        checksum = item.checksum,
        dock = item.dock,
        floorValue = "%.2f".format(item.floorValue),
        priority = item.priority.toString(),
        pinned = "${item.pinned}",
        durationMinutes = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLockerForecastViews(items: MutableList<LockerForecast>): MutableList<LockerForecastView> {
    val views = mutableListOf<LockerForecastView>()
    for (item in items) {
        views.add(toLockerForecastView(item))
    }
    return views
}

fun sumLockerForecastView(view: LockerForecastView): Double {
    val parsedFloorValue = view.floorValue.toDouble()
    val parsedPriority = view.priority.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedFloorValue + parsedPriority.toDouble() + parsedDurationMinutes.toDouble()
}
