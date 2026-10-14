package com.acme.transport.network.hub.mapping

/** How screens and exports show a hub exception. */
data class HubExceptionView(
    var code: String,
    var label: String,
    var rate: String,
    var country: String,
    var windowDays: String,
    var batchSizeText: String,
    var titleText: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toHubExceptionView(item: HubException): HubExceptionView {
    return HubExceptionView(
        code = item.code,
        label = item.label,
        rate = "%.2f".format(item.rate),
        country = item.country,
        windowDays = item.windowDays.toString(),
        batchSizeText = item.batchSize.toString(),
        titleText = item.title,
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubExceptionViews(items: MutableList<HubException>): MutableList<HubExceptionView> {
    val views = mutableListOf<HubExceptionView>()
    for (item in items) {
        views.add(toHubExceptionView(item))
    }
    return views
}

fun sumHubExceptionView(view: HubExceptionView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedWindowDays = view.windowDays.toInt()
    val parsedBatchSize = view.batchSizeText.toInt()
    return parsedRate + parsedWindowDays.toDouble() + parsedBatchSize.toDouble()
}
