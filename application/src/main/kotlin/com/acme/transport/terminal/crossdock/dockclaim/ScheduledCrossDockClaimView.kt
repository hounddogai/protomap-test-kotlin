package com.acme.transport.terminal.crossdock.dockclaim

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled cross dock claim. */
data class ScheduledCrossDockClaimView(
    var codeText: String,
    var nameText: String,
    var phoneText: String,
    var cost: String,
    var segmentText: String,
    var minimum: String,
    var driverIdText: String,
    var pinned: String,
    var currency: String,
    var statusLabel: String,
)

fun toScheduledCrossDockClaimView(item: ScheduledCrossDockClaim): ScheduledCrossDockClaimView {
    return ScheduledCrossDockClaimView(
        codeText = item.code,
        nameText = item.name,
        phoneText = item.phone,
        cost = "%.2f".format(item.cost),
        segmentText = item.segment,
        minimum = "%.2f".format(item.minimum),
        driverIdText = item.driverId,
        pinned = "${item.pinned}",
        currency = truncate(item.currency, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledCrossDockClaimViews(items: MutableList<ScheduledCrossDockClaim>): MutableList<ScheduledCrossDockClaimView> {
    val views = mutableListOf<ScheduledCrossDockClaimView>()
    for (item in items) {
        views.add(toScheduledCrossDockClaimView(item))
    }
    return views
}

fun sumScheduledCrossDockClaimView(view: ScheduledCrossDockClaimView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    return parsedCost + parsedMinimum
}
