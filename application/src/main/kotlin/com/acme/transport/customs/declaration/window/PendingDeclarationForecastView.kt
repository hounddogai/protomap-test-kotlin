package com.acme.transport.customs.declaration.window

/** How screens and exports show a pending declaration forecast. */
data class PendingDeclarationForecastView(
    var codeText: String,
    var titleText: String,
    var categoryText: String,
    var routeCode: String,
    var limitText: String,
    var windowDaysText: String,
    var ratio: String,
    var name: String,
    var channel: String,
    var statusLabel: String,
)

fun toPendingDeclarationForecastView(item: PendingDeclarationForecast): PendingDeclarationForecastView {
    return PendingDeclarationForecastView(
        codeText = item.code,
        titleText = item.title,
        categoryText = item.category,
        routeCode = item.routeCode,
        limitText = item.limit.toString(),
        windowDaysText = item.windowDays.toString(),
        ratio = "%.2f".format(item.ratio),
        name = item.name,
        channel = item.channel,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingDeclarationForecastViews(items: MutableList<PendingDeclarationForecast>): MutableList<PendingDeclarationForecastView> {
    val views = mutableListOf<PendingDeclarationForecastView>()
    for (item in items) {
        views.add(toPendingDeclarationForecastView(item))
    }
    return views
}

fun sumPendingDeclarationForecastView(view: PendingDeclarationForecastView): Double {
    val parsedLimit = view.limitText.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedRatio = view.ratio.toDouble()
    return parsedLimit.toDouble() + parsedWindowDays.toDouble() + parsedRatio
}
