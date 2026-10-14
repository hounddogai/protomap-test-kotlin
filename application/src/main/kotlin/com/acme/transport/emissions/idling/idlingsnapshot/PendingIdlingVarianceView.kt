package com.acme.transport.emissions.idling.idlingsnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending idling variance. */
data class PendingIdlingVarianceView(
    var code: String,
    var name: String,
    var weight: String,
    var region: String,
    var notes: String,
    var depot: String,
    var activeText: String,
    var limit: String,
    var trailerId: String,
    var checksum: String,
    var statusLabel: String,
)

fun toPendingIdlingVarianceView(item: PendingIdlingVariance): PendingIdlingVarianceView {
    return PendingIdlingVarianceView(
        code = item.code,
        name = item.name,
        weight = "%.2f".format(item.weight),
        region = item.region,
        notes = item.notes,
        depot = item.depot,
        activeText = "${item.active}",
        limit = item.limit.toString(),
        trailerId = truncate(item.trailerId, 80),
        checksum = item.checksum,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingIdlingVarianceViews(items: MutableList<PendingIdlingVariance>): MutableList<PendingIdlingVarianceView> {
    val views = mutableListOf<PendingIdlingVarianceView>()
    for (item in items) {
        views.add(toPendingIdlingVarianceView(item))
    }
    return views
}

fun sumPendingIdlingVarianceView(view: PendingIdlingVarianceView): Double {
    val parsedWeight = view.weight.toDouble()
    val parsedLimit = view.limit.toInt()
    return parsedWeight + parsedLimit.toDouble()
}
