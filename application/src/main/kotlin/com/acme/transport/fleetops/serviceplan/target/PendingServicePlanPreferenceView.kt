package com.acme.transport.fleetops.serviceplan.target

/** How screens and exports show a pending service plan preference. */
data class PendingServicePlanPreferenceView(
    var codeText: String,
    var title: String,
    var ceiling: String,
    var graceDaysText: String,
    var archivedText: String,
    var stopCount: String,
    var routeCode: String,
    var checksum: String,
    var statusLabel: String,
)

fun toPendingServicePlanPreferenceView(item: PendingServicePlanPreference): PendingServicePlanPreferenceView {
    return PendingServicePlanPreferenceView(
        codeText = item.code,
        title = item.title,
        ceiling = "%.2f".format(item.ceiling),
        graceDaysText = item.graceDays.toString(),
        archivedText = "${item.archived}",
        stopCount = item.stopCount.toString(),
        routeCode = item.routeCode,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServicePlanPreferenceViews(items: MutableList<PendingServicePlanPreference>): MutableList<PendingServicePlanPreferenceView> {
    val views = mutableListOf<PendingServicePlanPreferenceView>()
    for (item in items) {
        views.add(toPendingServicePlanPreferenceView(item))
    }
    return views
}

fun sumPendingServicePlanPreferenceView(view: PendingServicePlanPreferenceView): Double {
    val parsedCeiling = view.ceiling.toDouble()
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedCeiling + parsedGraceDays.toDouble() + parsedStopCount.toDouble()
}
