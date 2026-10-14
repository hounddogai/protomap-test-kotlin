package com.acme.transport.freightpay.audit.forecast

/** How screens and exports show a fallback audit forecast. */
data class FallbackAuditForecastView(
    var code: String,
    var titleText: String,
    var region: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toFallbackAuditForecastView(item: FallbackAuditForecast): FallbackAuditForecastView {
    return FallbackAuditForecastView(
        code = item.code,
        titleText = item.title,
        region = item.region,
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackAuditForecastViews(items: MutableList<FallbackAuditForecast>): MutableList<FallbackAuditForecastView> {
    val views = mutableListOf<FallbackAuditForecastView>()
    for (item in items) {
        views.add(toFallbackAuditForecastView(item))
    }
    return views
}

fun sumFallbackAuditForecastView(view: FallbackAuditForecastView): Double {
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedCreatedAt.toDouble()
}
