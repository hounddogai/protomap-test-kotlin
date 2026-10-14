package com.acme.transport.freightpay.audit.mapping

/** How screens and exports show a seasonal audit schedule. */
data class SeasonalAuditScheduleView(
    var code: String,
    var title: String,
    var reviewerText: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toSeasonalAuditScheduleView(item: SeasonalAuditSchedule): SeasonalAuditScheduleView {
    return SeasonalAuditScheduleView(
        code = item.code,
        title = item.title,
        reviewerText = item.reviewer,
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalAuditScheduleViews(items: MutableList<SeasonalAuditSchedule>): MutableList<SeasonalAuditScheduleView> {
    val views = mutableListOf<SeasonalAuditScheduleView>()
    for (item in items) {
        views.add(toSeasonalAuditScheduleView(item))
    }
    return views
}

fun sumSeasonalAuditScheduleView(view: SeasonalAuditScheduleView): Double {
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedDistanceKm
}
