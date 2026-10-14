package com.acme.transport.freightpay.dispute.assignment

/** How screens and exports show a dispute summary. */
data class DisputeSummaryView(
    var code: String,
    var label: String,
    var discount: String,
    var capacity: String,
    var driverId: String,
    var version: String,
    var statusLabel: String,
)

fun toDisputeSummaryView(item: DisputeSummary): DisputeSummaryView {
    return DisputeSummaryView(
        code = item.code,
        label = item.label,
        discount = "%.2f".format(item.discount),
        capacity = item.capacity.toString(),
        driverId = item.driverId,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDisputeSummaryViews(items: MutableList<DisputeSummary>): MutableList<DisputeSummaryView> {
    val views = mutableListOf<DisputeSummaryView>()
    for (item in items) {
        views.add(toDisputeSummaryView(item))
    }
    return views
}

fun sumDisputeSummaryView(view: DisputeSummaryView): Double {
    val parsedDiscount = view.discount.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedVersion = view.version.toInt()
    return parsedDiscount + parsedCapacity.toDouble() + parsedVersion.toDouble()
}
