package com.acme.transport.fleetops.leasereturn.returnrequest

/** How screens and exports show a adaptive lease return window. */
data class AdaptiveLeaseReturnWindowView(
    var code: String,
    var name: String,
    var amountText: String,
    var endsAt: String,
    var costText: String,
    var unit: String,
    var capacityText: String,
    var minimum: String,
    var statusLabel: String,
)

fun toAdaptiveLeaseReturnWindowView(item: AdaptiveLeaseReturnWindow): AdaptiveLeaseReturnWindowView {
    return AdaptiveLeaseReturnWindowView(
        code = item.code,
        name = item.name,
        amountText = "%.2f".format(item.amount),
        endsAt = item.endsAt.toString(),
        costText = "%.2f".format(item.cost),
        unit = item.unit,
        capacityText = item.capacity.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveLeaseReturnWindowViews(items: MutableList<AdaptiveLeaseReturnWindow>): MutableList<AdaptiveLeaseReturnWindowView> {
    val views = mutableListOf<AdaptiveLeaseReturnWindowView>()
    for (item in items) {
        views.add(toAdaptiveLeaseReturnWindowView(item))
    }
    return views
}

fun sumAdaptiveLeaseReturnWindowView(view: AdaptiveLeaseReturnWindowView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedCost = view.costText.toDouble()
    return parsedAmount + parsedEndsAt.toDouble() + parsedCost
}
