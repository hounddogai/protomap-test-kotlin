package com.acme.transport.terminal.crossdock.schedule

/** How screens and exports show a regional cross dock quota. */
data class RegionalCrossDockQuotaView(
    var codeText: String,
    var label: String,
    var contactEmail: String,
    var windowDays: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toRegionalCrossDockQuotaView(item: RegionalCrossDockQuota): RegionalCrossDockQuotaView {
    return RegionalCrossDockQuotaView(
        codeText = item.code,
        label = item.label,
        contactEmail = item.contactEmail,
        windowDays = item.windowDays.toString(),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalCrossDockQuotaViews(items: MutableList<RegionalCrossDockQuota>): MutableList<RegionalCrossDockQuotaView> {
    val views = mutableListOf<RegionalCrossDockQuotaView>()
    for (item in items) {
        views.add(toRegionalCrossDockQuotaView(item))
    }
    return views
}

fun sumRegionalCrossDockQuotaView(view: RegionalCrossDockQuotaView): Double {
    val parsedWindowDays = view.windowDays.toInt()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedWindowDays.toDouble() + parsedFloorValue
}
