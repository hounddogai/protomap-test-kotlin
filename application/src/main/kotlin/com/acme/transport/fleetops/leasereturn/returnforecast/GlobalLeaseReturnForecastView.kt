package com.acme.transport.fleetops.leasereturn.returnforecast

/** How screens and exports show a global lease return forecast. */
data class GlobalLeaseReturnForecastView(
    var codeText: String,
    var label: String,
    var retriesText: String,
    var contactEmailText: String,
    var margin: String,
    var trailerIdText: String,
    var lane: String,
    var phone: String,
    var endsAt: String,
    var dock: String,
    var statusLabel: String,
)

fun toGlobalLeaseReturnForecastView(item: GlobalLeaseReturnForecast): GlobalLeaseReturnForecastView {
    return GlobalLeaseReturnForecastView(
        codeText = item.code,
        label = item.label,
        retriesText = item.retries.toString(),
        contactEmailText = item.contactEmail,
        margin = "%.2f".format(item.margin),
        trailerIdText = item.trailerId,
        lane = item.lane,
        phone = item.phone,
        endsAt = item.endsAt.toString(),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalLeaseReturnForecastViews(items: MutableList<GlobalLeaseReturnForecast>): MutableList<GlobalLeaseReturnForecastView> {
    val views = mutableListOf<GlobalLeaseReturnForecastView>()
    for (item in items) {
        views.add(toGlobalLeaseReturnForecastView(item))
    }
    return views
}

fun sumGlobalLeaseReturnForecastView(view: GlobalLeaseReturnForecastView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    return parsedRetries.toDouble() + parsedMargin + parsedEndsAt.toDouble()
}
