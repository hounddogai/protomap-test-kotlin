package com.acme.transport.lastmile.redelivery.segment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a redelivery forecast. */
data class RedeliveryForecastView(
    var codeText: String,
    var title: String,
    var versionText: String,
    var country: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toRedeliveryForecastView(item: RedeliveryForecast): RedeliveryForecastView {
    return RedeliveryForecastView(
        codeText = item.code,
        title = truncate(item.title, 40),
        versionText = item.version.toString(),
        country = item.country,
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRedeliveryForecastViews(items: MutableList<RedeliveryForecast>): MutableList<RedeliveryForecastView> {
    val views = mutableListOf<RedeliveryForecastView>()
    for (item in items) {
        views.add(toRedeliveryForecastView(item))
    }
    return views
}

fun sumRedeliveryForecastView(view: RedeliveryForecastView): Double {
    val parsedVersion = view.versionText.toInt()
    return parsedVersion.toDouble()
}
