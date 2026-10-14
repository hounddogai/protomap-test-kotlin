package com.acme.transport.safety.scorecard.ledger

/** How screens and exports show a scorecard allocation. */
data class ScorecardAllocationView(
    var code: String,
    var title: String,
    var driverId: String,
    var country: String,
    var segmentText: String,
    var capacityText: String,
    var depot: String,
    var durationMinutes: String,
    var routeCode: String,
    var statusLabel: String,
)

fun toScorecardAllocationView(item: ScorecardAllocation): ScorecardAllocationView {
    return ScorecardAllocationView(
        code = item.code,
        title = item.title,
        driverId = item.driverId,
        country = item.country,
        segmentText = item.segment,
        capacityText = item.capacity.toString(),
        depot = item.depot,
        durationMinutes = item.durationMinutes.toString(),
        routeCode = item.routeCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScorecardAllocationViews(items: MutableList<ScorecardAllocation>): MutableList<ScorecardAllocationView> {
    val views = mutableListOf<ScorecardAllocationView>()
    for (item in items) {
        views.add(toScorecardAllocationView(item))
    }
    return views
}

fun sumScorecardAllocationView(view: ScorecardAllocationView): Double {
    val parsedCapacity = view.capacityText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedCapacity.toDouble() + parsedDurationMinutes.toDouble()
}
