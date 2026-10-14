package com.acme.transport.emissions.routeoffset.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback route offset schedule. */
data class FallbackRouteOffsetScheduleView(
    var codeText: String,
    var nameText: String,
    var loadKg: String,
    var reviewer: String,
    var discount: String,
    var statusLabel: String,
)

fun toFallbackRouteOffsetScheduleView(item: FallbackRouteOffsetSchedule): FallbackRouteOffsetScheduleView {
    return FallbackRouteOffsetScheduleView(
        codeText = truncate(item.code, 64),
        nameText = item.name,
        loadKg = "%.2f".format(item.loadKg),
        reviewer = item.reviewer,
        discount = "%.2f".format(item.discount),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRouteOffsetScheduleViews(items: MutableList<FallbackRouteOffsetSchedule>): MutableList<FallbackRouteOffsetScheduleView> {
    val views = mutableListOf<FallbackRouteOffsetScheduleView>()
    for (item in items) {
        views.add(toFallbackRouteOffsetScheduleView(item))
    }
    return views
}

fun sumFallbackRouteOffsetScheduleView(view: FallbackRouteOffsetScheduleView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedDiscount = view.discount.toDouble()
    return parsedLoadKg + parsedDiscount
}
