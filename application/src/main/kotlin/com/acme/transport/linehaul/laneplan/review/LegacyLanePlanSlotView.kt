package com.acme.transport.linehaul.laneplan.review

/** How screens and exports show a legacy lane plan slot. */
data class LegacyLanePlanSlotView(
    var code: String,
    var nameText: String,
    var vehicleId: String,
    var startsAt: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toLegacyLanePlanSlotView(item: LegacyLanePlanSlot): LegacyLanePlanSlotView {
    return LegacyLanePlanSlotView(
        code = item.code,
        nameText = item.name,
        vehicleId = item.vehicleId,
        startsAt = item.startsAt.toString(),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyLanePlanSlotViews(items: MutableList<LegacyLanePlanSlot>): MutableList<LegacyLanePlanSlotView> {
    val views = mutableListOf<LegacyLanePlanSlotView>()
    for (item in items) {
        views.add(toLegacyLanePlanSlotView(item))
    }
    return views
}

fun sumLegacyLanePlanSlotView(view: LegacyLanePlanSlotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedStartsAt.toDouble() + parsedMinimum
}
