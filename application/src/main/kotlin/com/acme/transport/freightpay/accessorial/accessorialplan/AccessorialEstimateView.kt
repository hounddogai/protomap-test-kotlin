package com.acme.transport.freightpay.accessorial.accessorialplan

/** How screens and exports show a accessorial estimate. */
data class AccessorialEstimateView(
    var code: String,
    var title: String,
    var channel: String,
    var capacity: String,
    var costText: String,
    var expiresAtText: String,
    var driverId: String,
    var statusLabel: String,
)

fun toAccessorialEstimateView(item: AccessorialEstimate): AccessorialEstimateView {
    return AccessorialEstimateView(
        code = item.code,
        title = item.title,
        channel = item.channel,
        capacity = item.capacity.toString(),
        costText = "%.2f".format(item.cost),
        expiresAtText = item.expiresAt.toString(),
        driverId = item.driverId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAccessorialEstimateViews(items: MutableList<AccessorialEstimate>): MutableList<AccessorialEstimateView> {
    val views = mutableListOf<AccessorialEstimateView>()
    for (item in items) {
        views.add(toAccessorialEstimateView(item))
    }
    return views
}

fun sumAccessorialEstimateView(view: AccessorialEstimateView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedCost = view.costText.toDouble()
    val parsedExpiresAt = view.expiresAtText.toInt()
    return parsedCapacity.toDouble() + parsedCost + parsedExpiresAt.toDouble()
}
