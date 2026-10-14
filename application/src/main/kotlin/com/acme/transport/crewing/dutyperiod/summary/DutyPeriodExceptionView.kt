package com.acme.transport.crewing.dutyperiod.summary

/** How screens and exports show a duty period exception. */
data class DutyPeriodExceptionView(
    var code: String,
    var labelText: String,
    var source: String,
    var graceDaysText: String,
    var title: String,
    var updatedAt: String,
    var timezone: String,
    var rate: String,
    var active: String,
    var thresholdText: String,
    var cost: String,
    var statusLabel: String,
)

fun toDutyPeriodExceptionView(item: DutyPeriodException): DutyPeriodExceptionView {
    return DutyPeriodExceptionView(
        code = item.code,
        labelText = item.label,
        source = item.source,
        graceDaysText = item.graceDays.toString(),
        title = item.title,
        updatedAt = item.updatedAt.toString(),
        timezone = item.timezone,
        rate = "%.2f".format(item.rate),
        active = "${item.active}",
        thresholdText = "%.2f".format(item.threshold),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDutyPeriodExceptionViews(items: MutableList<DutyPeriodException>): MutableList<DutyPeriodExceptionView> {
    val views = mutableListOf<DutyPeriodExceptionView>()
    for (item in items) {
        views.add(toDutyPeriodExceptionView(item))
    }
    return views
}

fun sumDutyPeriodExceptionView(view: DutyPeriodExceptionView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedGraceDays.toDouble() + parsedUpdatedAt.toDouble() + parsedRate
}
