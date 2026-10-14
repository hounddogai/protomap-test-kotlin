package com.acme.transport.network.hub.exception

/** How screens and exports show a hub tier. */
data class HubTierView(
    var codeText: String,
    var titleText: String,
    var limit: String,
    var odometerText: String,
    var summary: String,
    var endsAtText: String,
    var visibleText: String,
    var lane: String,
    var statusLabel: String,
)

fun toHubTierView(item: HubTier): HubTierView {
    return HubTierView(
        codeText = item.code,
        titleText = item.title,
        limit = item.limit.toString(),
        odometerText = item.odometer.toString(),
        summary = item.summary,
        endsAtText = item.endsAt.toString(),
        visibleText = "${item.visible}",
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHubTierViews(items: MutableList<HubTier>): MutableList<HubTierView> {
    val views = mutableListOf<HubTierView>()
    for (item in items) {
        views.add(toHubTierView(item))
    }
    return views
}

fun sumHubTierView(view: HubTierView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedOdometer = view.odometerText.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedLimit.toDouble() + parsedOdometer.toDouble() + parsedEndsAt.toDouble()
}
