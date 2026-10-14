package com.acme.transport.lastmile.drop.dropforecast

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional drop segment. */
data class RegionalDropSegmentView(
    var code: String,
    var name: String,
    var priority: String,
    var routeCode: String,
    var phone: String,
    var statusLabel: String,
)

fun toRegionalDropSegmentView(item: RegionalDropSegment): RegionalDropSegmentView {
    return RegionalDropSegmentView(
        code = item.code,
        name = item.name,
        priority = item.priority.toString(),
        routeCode = truncate(item.routeCode, 120),
        phone = truncate(item.phone, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDropSegmentViews(items: MutableList<RegionalDropSegment>): MutableList<RegionalDropSegmentView> {
    val views = mutableListOf<RegionalDropSegmentView>()
    for (item in items) {
        views.add(toRegionalDropSegmentView(item))
    }
    return views
}

fun sumRegionalDropSegmentView(view: RegionalDropSegmentView): Double {
    val parsedPriority = view.priority.toInt()
    return parsedPriority.toDouble()
}
