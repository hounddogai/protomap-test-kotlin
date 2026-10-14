package com.acme.transport.linehaul.laneplan.forecast

/** How screens and exports show a lane plan forecast. */
data class LanePlanForecastView(
    var code: String,
    var title: String,
    var category: String,
    var shiftCode: String,
    var priority: String,
    var maximum: String,
    var source: String,
    var locale: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toLanePlanForecastView(item: LanePlanForecast): LanePlanForecastView {
    return LanePlanForecastView(
        code = item.code,
        title = item.title,
        category = item.category,
        shiftCode = item.shiftCode,
        priority = item.priority.toString(),
        maximum = "%.2f".format(item.maximum),
        source = item.source,
        locale = item.locale,
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanForecastViews(items: MutableList<LanePlanForecast>): MutableList<LanePlanForecastView> {
    val views = mutableListOf<LanePlanForecastView>()
    for (item in items) {
        views.add(toLanePlanForecastView(item))
    }
    return views
}

fun sumLanePlanForecastView(view: LanePlanForecastView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedMaximum = view.maximum.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedPriority.toDouble() + parsedMaximum + parsedCreatedAt.toDouble()
}
