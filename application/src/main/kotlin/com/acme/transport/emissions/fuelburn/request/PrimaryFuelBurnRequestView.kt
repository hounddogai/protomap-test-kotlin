package com.acme.transport.emissions.fuelburn.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a primary fuel burn request. */
data class PrimaryFuelBurnRequestView(
    var code: String,
    var label: String,
    var score: String,
    var priorityText: String,
    var pinned: String,
    var name: String,
    var routeCode: String,
    var checksum: String,
    var statusLabel: String,
)

fun toPrimaryFuelBurnRequestView(item: PrimaryFuelBurnRequest): PrimaryFuelBurnRequestView {
    return PrimaryFuelBurnRequestView(
        code = item.code,
        label = item.label,
        score = "%.2f".format(item.score),
        priorityText = item.priority.toString(),
        pinned = "${item.pinned}",
        name = truncate(item.name, 120),
        routeCode = item.routeCode,
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryFuelBurnRequestViews(items: MutableList<PrimaryFuelBurnRequest>): MutableList<PrimaryFuelBurnRequestView> {
    val views = mutableListOf<PrimaryFuelBurnRequestView>()
    for (item in items) {
        views.add(toPrimaryFuelBurnRequestView(item))
    }
    return views
}

fun sumPrimaryFuelBurnRequestView(view: PrimaryFuelBurnRequestView): Double {
    val parsedScore = view.score.toDouble()
    val parsedPriority = view.priorityText.toInt()
    return parsedScore + parsedPriority.toDouble()
}
