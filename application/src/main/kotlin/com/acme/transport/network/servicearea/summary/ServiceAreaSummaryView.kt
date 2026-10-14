package com.acme.transport.network.servicearea.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area summary. */
data class ServiceAreaSummaryView(
    var code: String,
    var name: String,
    var endsAtText: String,
    var stopCountText: String,
    var archivedText: String,
    var routeCodeText: String,
    var statusLabel: String,
)

fun toServiceAreaSummaryView(item: ServiceAreaSummary): ServiceAreaSummaryView {
    return ServiceAreaSummaryView(
        code = truncate(item.code, 40),
        name = item.name,
        endsAtText = item.endsAt.toString(),
        stopCountText = item.stopCount.toString(),
        archivedText = "${item.archived}",
        routeCodeText = truncate(item.routeCode, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaSummaryViews(items: MutableList<ServiceAreaSummary>): MutableList<ServiceAreaSummaryView> {
    val views = mutableListOf<ServiceAreaSummaryView>()
    for (item in items) {
        views.add(toServiceAreaSummaryView(item))
    }
    return views
}

fun sumServiceAreaSummaryView(view: ServiceAreaSummaryView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    return parsedEndsAt.toDouble() + parsedStopCount.toDouble()
}
