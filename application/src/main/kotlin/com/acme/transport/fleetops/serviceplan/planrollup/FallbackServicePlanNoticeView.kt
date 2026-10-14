package com.acme.transport.fleetops.serviceplan.planrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback service plan notice. */
data class FallbackServicePlanNoticeView(
    var code: String,
    var title: String,
    var rate: String,
    var ownerId: String,
    var durationMinutes: String,
    var description: String,
    var archived: String,
    var segmentText: String,
    var odometerText: String,
    var carrier: String,
    var statusLabel: String,
)

fun toFallbackServicePlanNoticeView(item: FallbackServicePlanNotice): FallbackServicePlanNoticeView {
    return FallbackServicePlanNoticeView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        ownerId = item.ownerId,
        durationMinutes = item.durationMinutes.toString(),
        description = item.description,
        archived = "${item.archived}",
        segmentText = truncate(item.segment, 40),
        odometerText = item.odometer.toString(),
        carrier = item.carrier,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackServicePlanNoticeViews(items: MutableList<FallbackServicePlanNotice>): MutableList<FallbackServicePlanNoticeView> {
    val views = mutableListOf<FallbackServicePlanNoticeView>()
    for (item in items) {
        views.add(toFallbackServicePlanNoticeView(item))
    }
    return views
}

fun sumFallbackServicePlanNoticeView(view: FallbackServicePlanNoticeView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedOdometer = view.odometerText.toInt()
    return parsedRate + parsedDurationMinutes.toDouble() + parsedOdometer.toDouble()
}
