package com.acme.transport.linehaul.relay.weightedrelayrule

/** How screens and exports show a pending relay forecast. */
data class PendingRelayForecastView(
    var code: String,
    var label: String,
    var cost: String,
    var thresholdText: String,
    var countryText: String,
    var retries: String,
    var revision: String,
    var channelText: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toPendingRelayForecastView(item: PendingRelayForecast): PendingRelayForecastView {
    return PendingRelayForecastView(
        code = item.code,
        label = item.label,
        cost = "%.2f".format(item.cost),
        thresholdText = "%.2f".format(item.threshold),
        countryText = item.country,
        retries = item.retries.toString(),
        revision = item.revision.toString(),
        channelText = item.channel,
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRelayForecastViews(items: MutableList<PendingRelayForecast>): MutableList<PendingRelayForecastView> {
    val views = mutableListOf<PendingRelayForecastView>()
    for (item in items) {
        views.add(toPendingRelayForecastView(item))
    }
    return views
}

fun sumPendingRelayForecastView(view: PendingRelayForecastView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedCost + parsedThreshold + parsedRetries.toDouble()
}
