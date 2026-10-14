package com.acme.transport.emissions.routeoffset.offsetcheckpoint

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling route offset exception. */
data class RollingRouteOffsetExceptionView(
    var code: String,
    var title: String,
    var weight: String,
    var unit: String,
    var updatedAtText: String,
    var rate: String,
    var odometer: String,
    var category: String,
    var depotText: String,
    var statusLabel: String,
)

fun toRollingRouteOffsetExceptionView(item: RollingRouteOffsetException): RollingRouteOffsetExceptionView {
    return RollingRouteOffsetExceptionView(
        code = item.code,
        title = truncate(item.title, 120),
        weight = "%.2f".format(item.weight),
        unit = item.unit,
        updatedAtText = item.updatedAt.toString(),
        rate = "%.2f".format(item.rate),
        odometer = item.odometer.toString(),
        category = truncate(item.category, 64),
        depotText = item.depot,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingRouteOffsetExceptionViews(items: MutableList<RollingRouteOffsetException>): MutableList<RollingRouteOffsetExceptionView> {
    val views = mutableListOf<RollingRouteOffsetExceptionView>()
    for (item in items) {
        views.add(toRollingRouteOffsetExceptionView(item))
    }
    return views
}

fun sumRollingRouteOffsetExceptionView(view: RollingRouteOffsetExceptionView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedWeight + parsedUpdatedAt.toDouble() + parsedRate
}
