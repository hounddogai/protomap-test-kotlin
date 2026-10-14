package com.acme.transport.lastmile.redelivery.metric

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default redelivery snapshot. */
data class DefaultRedeliverySnapshotView(
    var code: String,
    var titleText: String,
    var approved: String,
    var depot: String,
    var startsAt: String,
    var distanceKm: String,
    var label: String,
    var carrier: String,
    var statusLabel: String,
)

fun toDefaultRedeliverySnapshotView(item: DefaultRedeliverySnapshot): DefaultRedeliverySnapshotView {
    return DefaultRedeliverySnapshotView(
        code = item.code,
        titleText = item.title,
        approved = "${item.approved}",
        depot = item.depot,
        startsAt = item.startsAt.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        label = item.label,
        carrier = truncate(item.carrier, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultRedeliverySnapshotViews(items: MutableList<DefaultRedeliverySnapshot>): MutableList<DefaultRedeliverySnapshotView> {
    val views = mutableListOf<DefaultRedeliverySnapshotView>()
    for (item in items) {
        views.add(toDefaultRedeliverySnapshotView(item))
    }
    return views
}

fun sumDefaultRedeliverySnapshotView(view: DefaultRedeliverySnapshotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedDistanceKm = view.distanceKm.toDouble()
    return parsedStartsAt.toDouble() + parsedDistanceKm
}
