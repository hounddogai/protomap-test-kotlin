package com.acme.transport.fleetops.tire.tiremanifest

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered tire adjustment. */
data class TieredTireAdjustmentView(
    var code: String,
    var label: String,
    var startsAt: String,
    var pinned: String,
    var createdAt: String,
    var reviewer: String,
    var statusLabel: String,
)

fun toTieredTireAdjustmentView(item: TieredTireAdjustment): TieredTireAdjustmentView {
    return TieredTireAdjustmentView(
        code = item.code,
        label = truncate(item.label, 40),
        startsAt = item.startsAt.toString(),
        pinned = "${item.pinned}",
        createdAt = item.createdAt.toString(),
        reviewer = item.reviewer,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredTireAdjustmentViews(items: MutableList<TieredTireAdjustment>): MutableList<TieredTireAdjustmentView> {
    val views = mutableListOf<TieredTireAdjustmentView>()
    for (item in items) {
        views.add(toTieredTireAdjustmentView(item))
    }
    return views
}

fun sumTieredTireAdjustmentView(view: TieredTireAdjustmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedStartsAt.toDouble() + parsedCreatedAt.toDouble()
}
