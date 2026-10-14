package com.acme.transport.network.zone.zonegroup

/** How screens and exports show a zone adjustment. */
data class ZoneAdjustmentView(
    var codeText: String,
    var titleText: String,
    var endsAt: String,
    var startsAtText: String,
    var activeText: String,
    var shiftCode: String,
    var lane: String,
    var pinned: String,
    var rate: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toZoneAdjustmentView(item: ZoneAdjustment): ZoneAdjustmentView {
    return ZoneAdjustmentView(
        codeText = item.code,
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        startsAtText = item.startsAt.toString(),
        activeText = "${item.active}",
        shiftCode = item.shiftCode,
        lane = item.lane,
        pinned = "${item.pinned}",
        rate = "%.2f".format(item.rate),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toZoneAdjustmentViews(items: MutableList<ZoneAdjustment>): MutableList<ZoneAdjustmentView> {
    val views = mutableListOf<ZoneAdjustmentView>()
    for (item in items) {
        views.add(toZoneAdjustmentView(item))
    }
    return views
}

fun sumZoneAdjustmentView(view: ZoneAdjustmentView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedStartsAt = view.startsAtText.toInt()
    val parsedRate = view.rate.toDouble()
    return parsedEndsAt.toDouble() + parsedStartsAt.toDouble() + parsedRate
}
