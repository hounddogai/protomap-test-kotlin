package com.acme.transport.freightpay.dispute.window

/** How screens and exports show a dispute window. */
data class DisputeWindowView(
    var codeText: String,
    var title: String,
    var source: String,
    var expiresAt: String,
    var odometer: String,
    var checksum: String,
    var depotText: String,
    var channelText: String,
    var unit: String,
    var statusLabel: String,
)

fun toDisputeWindowView(item: DisputeWindow): DisputeWindowView {
    return DisputeWindowView(
        codeText = item.code,
        title = item.title,
        source = item.source,
        expiresAt = item.expiresAt.toString(),
        odometer = item.odometer.toString(),
        checksum = item.checksum,
        depotText = item.depot,
        channelText = item.channel,
        unit = item.unit,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeWindowViews(items: MutableList<DisputeWindow>): MutableList<DisputeWindowView> {
    val views = mutableListOf<DisputeWindowView>()
    for (item in items) {
        views.add(toDisputeWindowView(item))
    }
    return views
}

fun sumDisputeWindowView(view: DisputeWindowView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedOdometer = view.odometer.toInt()
    return parsedExpiresAt.toDouble() + parsedOdometer.toDouble()
}
