package com.acme.transport.fleetops.serviceplan.forecast

/** How screens and exports show a scheduled service plan forecast. */
data class ScheduledServicePlanForecastView(
    var code: String,
    var label: String,
    var countryText: String,
    var attempts: String,
    var batchSize: String,
    var endsAtText: String,
    var archivedText: String,
    var graceDaysText: String,
    var discount: String,
    var rate: String,
    var statusLabel: String,
)

fun toScheduledServicePlanForecastView(item: ScheduledServicePlanForecast): ScheduledServicePlanForecastView {
    return ScheduledServicePlanForecastView(
        code = item.code,
        label = item.label,
        countryText = item.country,
        attempts = item.attempts.toString(),
        batchSize = item.batchSize.toString(),
        endsAtText = item.endsAt.toString(),
        archivedText = "${item.archived}",
        graceDaysText = item.graceDays.toString(),
        discount = "%.2f".format(item.discount),
        rate = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledServicePlanForecastViews(items: MutableList<ScheduledServicePlanForecast>): MutableList<ScheduledServicePlanForecastView> {
    val views = mutableListOf<ScheduledServicePlanForecastView>()
    for (item in items) {
        views.add(toScheduledServicePlanForecastView(item))
    }
    return views
}

fun sumScheduledServicePlanForecastView(view: ScheduledServicePlanForecastView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedAttempts.toDouble() + parsedBatchSize.toDouble() + parsedEndsAt.toDouble()
}
