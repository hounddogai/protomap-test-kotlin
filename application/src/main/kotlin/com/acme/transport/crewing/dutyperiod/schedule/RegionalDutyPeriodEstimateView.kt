package com.acme.transport.crewing.dutyperiod.schedule

/** How screens and exports show a regional duty period estimate. */
data class RegionalDutyPeriodEstimateView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var distanceKm: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toRegionalDutyPeriodEstimateView(item: RegionalDutyPeriodEstimate): RegionalDutyPeriodEstimateView {
    return RegionalDutyPeriodEstimateView(
        code = item.code,
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalDutyPeriodEstimateViews(items: MutableList<RegionalDutyPeriodEstimate>): MutableList<RegionalDutyPeriodEstimateView> {
    val views = mutableListOf<RegionalDutyPeriodEstimateView>()
    for (item in items) {
        views.add(toRegionalDutyPeriodEstimateView(item))
    }
    return views
}

fun sumRegionalDutyPeriodEstimateView(view: RegionalDutyPeriodEstimateView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedUpdatedAt.toDouble() + parsedDistanceKm
}
