package com.acme.transport.crewing.dutyperiod.estimate

/** How screens and exports show a scheduled duty period estimate. */
data class ScheduledDutyPeriodEstimateView(
    var codeText: String,
    var title: String,
    var graceDaysText: String,
    var weight: String,
    var locale: String,
    var checksumText: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toScheduledDutyPeriodEstimateView(item: ScheduledDutyPeriodEstimate): ScheduledDutyPeriodEstimateView {
    return ScheduledDutyPeriodEstimateView(
        codeText = item.code,
        title = item.title,
        graceDaysText = item.graceDays.toString(),
        weight = "%.2f".format(item.weight),
        locale = item.locale,
        checksumText = item.checksum,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledDutyPeriodEstimateViews(items: MutableList<ScheduledDutyPeriodEstimate>): MutableList<ScheduledDutyPeriodEstimateView> {
    val views = mutableListOf<ScheduledDutyPeriodEstimateView>()
    for (item in items) {
        views.add(toScheduledDutyPeriodEstimateView(item))
    }
    return views
}

fun sumScheduledDutyPeriodEstimateView(view: ScheduledDutyPeriodEstimateView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedGraceDays.toDouble() + parsedWeight + parsedCreatedAt.toDouble()
}
