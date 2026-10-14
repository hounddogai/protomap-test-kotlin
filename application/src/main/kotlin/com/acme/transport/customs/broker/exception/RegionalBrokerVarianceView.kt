package com.acme.transport.customs.broker.exception

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional broker variance. */
data class RegionalBrokerVarianceView(
    var code: String,
    var title: String,
    var expiresAtText: String,
    var lane: String,
    var limit: String,
    var routeCode: String,
    var etaMinutes: String,
    var thresholdText: String,
    var odometer: String,
    var approvedText: String,
    var statusLabel: String,
)

fun toRegionalBrokerVarianceView(item: RegionalBrokerVariance): RegionalBrokerVarianceView {
    return RegionalBrokerVarianceView(
        code = truncate(item.code, 80),
        title = item.title,
        expiresAtText = item.expiresAt.toString(),
        lane = item.lane,
        limit = item.limit.toString(),
        routeCode = item.routeCode,
        etaMinutes = item.etaMinutes.toString(),
        thresholdText = "%.2f".format(item.threshold),
        odometer = item.odometer.toString(),
        approvedText = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalBrokerVarianceViews(items: MutableList<RegionalBrokerVariance>): MutableList<RegionalBrokerVarianceView> {
    val views = mutableListOf<RegionalBrokerVarianceView>()
    for (item in items) {
        views.add(toRegionalBrokerVarianceView(item))
    }
    return views
}

fun sumRegionalBrokerVarianceView(view: RegionalBrokerVarianceView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedLimit = view.limit.toInt()
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedLimit.toDouble() + parsedEtaMinutes.toDouble()
}
