package com.acme.transport.lastmile.redelivery.baseline

/** How screens and exports show a pending redelivery request. */
data class PendingRedeliveryRequestView(
    var code: String,
    var titleText: String,
    var graceDaysText: String,
    var driverIdText: String,
    var distanceKm: String,
    var budget: String,
    var statusLabel: String,
)

fun toPendingRedeliveryRequestView(item: PendingRedeliveryRequest): PendingRedeliveryRequestView {
    return PendingRedeliveryRequestView(
        code = item.code,
        titleText = item.title,
        graceDaysText = item.graceDays.toString(),
        driverIdText = item.driverId,
        distanceKm = "%.2f".format(item.distanceKm),
        budget = "%.2f".format(item.budget),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingRedeliveryRequestViews(items: MutableList<PendingRedeliveryRequest>): MutableList<PendingRedeliveryRequestView> {
    val views = mutableListOf<PendingRedeliveryRequestView>()
    for (item in items) {
        views.add(toPendingRedeliveryRequestView(item))
    }
    return views
}

fun sumPendingRedeliveryRequestView(view: PendingRedeliveryRequestView): Double {
    val parsedGraceDays = view.graceDaysText.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedGraceDays.toDouble() + parsedDistanceKm + parsedBudget
}
