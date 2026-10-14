package com.acme.transport.customs.clearance.target

/** How screens and exports show a clearance claim. */
data class ClearanceClaimView(
    var code: String,
    var label: String,
    var notes: String,
    var costText: String,
    var capacity: String,
    var rate: String,
    var startsAtText: String,
    var statusLabel: String,
)

fun toClearanceClaimView(item: ClearanceClaim): ClearanceClaimView {
    return ClearanceClaimView(
        code = item.code,
        label = item.label,
        notes = item.notes,
        costText = "%.2f".format(item.cost),
        capacity = item.capacity.toString(),
        rate = "%.2f".format(item.rate),
        startsAtText = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toClearanceClaimViews(items: MutableList<ClearanceClaim>): MutableList<ClearanceClaimView> {
    val views = mutableListOf<ClearanceClaimView>()
    for (item in items) {
        views.add(toClearanceClaimView(item))
    }
    return views
}

fun sumClearanceClaimView(view: ClearanceClaimView): Double {
    val parsedCost = view.costText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedCost + parsedCapacity.toDouble() + parsedRate
}
