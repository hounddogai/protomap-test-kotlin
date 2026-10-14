package com.acme.transport.emissions.routeoffset.mapping

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback route offset profile. */
data class FallbackRouteOffsetProfileView(
    var code: String,
    var titleText: String,
    var approved: String,
    var routeCode: String,
    var weight: String,
    var statusLabel: String,
)

fun toFallbackRouteOffsetProfileView(item: FallbackRouteOffsetProfile): FallbackRouteOffsetProfileView {
    return FallbackRouteOffsetProfileView(
        code = item.code,
        titleText = item.title,
        approved = "${item.approved}",
        routeCode = truncate(item.routeCode, 40),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackRouteOffsetProfileViews(items: MutableList<FallbackRouteOffsetProfile>): MutableList<FallbackRouteOffsetProfileView> {
    val views = mutableListOf<FallbackRouteOffsetProfileView>()
    for (item in items) {
        views.add(toFallbackRouteOffsetProfileView(item))
    }
    return views
}

fun sumFallbackRouteOffsetProfileView(view: FallbackRouteOffsetProfileView): Double {
    val parsedWeight = view.weight.toDouble()
    return parsedWeight
}
