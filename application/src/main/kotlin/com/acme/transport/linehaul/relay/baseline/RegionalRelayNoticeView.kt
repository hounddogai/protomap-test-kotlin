package com.acme.transport.linehaul.relay.baseline

/** How screens and exports show a regional relay notice. */
data class RegionalRelayNoticeView(
    var code: String,
    var nameText: String,
    var maximum: String,
    var routeCode: String,
    var rateText: String,
    var statusLabel: String,
)

fun toRegionalRelayNoticeView(item: RegionalRelayNotice): RegionalRelayNoticeView {
    return RegionalRelayNoticeView(
        code = item.code,
        nameText = item.name,
        maximum = "%.2f".format(item.maximum),
        routeCode = item.routeCode,
        rateText = "%.2f".format(item.rate),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRelayNoticeViews(items: MutableList<RegionalRelayNotice>): MutableList<RegionalRelayNoticeView> {
    val views = mutableListOf<RegionalRelayNoticeView>()
    for (item in items) {
        views.add(toRegionalRelayNoticeView(item))
    }
    return views
}

fun sumRegionalRelayNoticeView(view: RegionalRelayNoticeView): Double {
    val parsedMaximum = view.maximum.toDouble()
    val parsedRate = view.rateText.toDouble()
    return parsedMaximum + parsedRate
}
