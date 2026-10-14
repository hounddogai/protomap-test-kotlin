package com.acme.transport.customs.broker.threshold

/** How screens and exports show a scheduled broker threshold. */
data class ScheduledBrokerThresholdView(
    var codeText: String,
    var title: String,
    var loadKg: String,
    var name: String,
    var graceDays: String,
    var currencyText: String,
    var statusLabel: String,
)

fun toScheduledBrokerThresholdView(item: ScheduledBrokerThreshold): ScheduledBrokerThresholdView {
    return ScheduledBrokerThresholdView(
        codeText = item.code,
        title = item.title,
        loadKg = "%.2f".format(item.loadKg),
        name = item.name,
        graceDays = item.graceDays.toString(),
        currencyText = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledBrokerThresholdViews(items: MutableList<ScheduledBrokerThreshold>): MutableList<ScheduledBrokerThresholdView> {
    val views = mutableListOf<ScheduledBrokerThresholdView>()
    for (item in items) {
        views.add(toScheduledBrokerThresholdView(item))
    }
    return views
}

fun sumScheduledBrokerThresholdView(view: ScheduledBrokerThresholdView): Double {
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedLoadKg + parsedGraceDays.toDouble()
}
