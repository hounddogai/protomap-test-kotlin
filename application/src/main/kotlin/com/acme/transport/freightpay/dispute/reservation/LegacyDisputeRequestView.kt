package com.acme.transport.freightpay.dispute.reservation

/** How screens and exports show a legacy dispute request. */
data class LegacyDisputeRequestView(
    var code: String,
    var label: String,
    var odometer: String,
    var budget: String,
    var floorValue: String,
    var statusLabel: String,
)

fun toLegacyDisputeRequestView(item: LegacyDisputeRequest): LegacyDisputeRequestView {
    return LegacyDisputeRequestView(
        code = item.code,
        label = item.label,
        odometer = item.odometer.toString(),
        budget = "%.2f".format(item.budget),
        floorValue = "%.2f".format(item.floorValue),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyDisputeRequestViews(items: MutableList<LegacyDisputeRequest>): MutableList<LegacyDisputeRequestView> {
    val views = mutableListOf<LegacyDisputeRequestView>()
    for (item in items) {
        views.add(toLegacyDisputeRequestView(item))
    }
    return views
}

fun sumLegacyDisputeRequestView(view: LegacyDisputeRequestView): Double {
    val parsedOdometer = view.odometer.toInt()
    val parsedBudget = view.budget.toDouble()
    val parsedFloorValue = view.floorValue.toDouble()
    return parsedOdometer.toDouble() + parsedBudget + parsedFloorValue
}
