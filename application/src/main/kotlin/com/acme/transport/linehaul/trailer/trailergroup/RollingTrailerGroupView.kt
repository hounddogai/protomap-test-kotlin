package com.acme.transport.linehaul.trailer.trailergroup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling trailer group. */
data class RollingTrailerGroupView(
    var code: String,
    var label: String,
    var depot: String,
    var vehicleId: String,
    var fuelLiters: String,
    var statusLabel: String,
)

fun toRollingTrailerGroupView(item: RollingTrailerGroup): RollingTrailerGroupView {
    return RollingTrailerGroupView(
        code = truncate(item.code, 120),
        label = item.label,
        depot = truncate(item.depot, 64),
        vehicleId = item.vehicleId,
        fuelLiters = "%.2f".format(item.fuelLiters),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingTrailerGroupViews(items: MutableList<RollingTrailerGroup>): MutableList<RollingTrailerGroupView> {
    val views = mutableListOf<RollingTrailerGroupView>()
    for (item in items) {
        views.add(toRollingTrailerGroupView(item))
    }
    return views
}

fun sumRollingTrailerGroupView(view: RollingTrailerGroupView): Double {
    val parsedFuelLiters = view.fuelLiters.toDouble()
    return parsedFuelLiters
}
