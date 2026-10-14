package com.acme.transport.fleetops.serviceplan.checkpoint

/** How screens and exports show a capped service plan forecast. */
data class CappedServicePlanForecastView(
    var code: String,
    var title: String,
    var batchSize: String,
    var contactEmailText: String,
    var shiftCode: String,
    var dockText: String,
    var statusLabel: String,
)

fun toCappedServicePlanForecastView(item: CappedServicePlanForecast): CappedServicePlanForecastView {
    return CappedServicePlanForecastView(
        code = item.code,
        title = item.title,
        batchSize = item.batchSize.toString(),
        contactEmailText = item.contactEmail,
        shiftCode = item.shiftCode,
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedServicePlanForecastViews(items: MutableList<CappedServicePlanForecast>): MutableList<CappedServicePlanForecastView> {
    val views = mutableListOf<CappedServicePlanForecastView>()
    for (item in items) {
        views.add(toCappedServicePlanForecastView(item))
    }
    return views
}

fun sumCappedServicePlanForecastView(view: CappedServicePlanForecastView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
