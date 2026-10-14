package com.acme.transport.crewing.availability.scheduledavailabilitytier

/** How screens and exports show a capped availability slot. */
data class CappedAvailabilitySlotView(
    var code: String,
    var titleText: String,
    var dockText: String,
    var durationMinutesText: String,
    var weight: String,
    var approved: String,
    var archived: String,
    var margin: String,
    var channelText: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toCappedAvailabilitySlotView(item: CappedAvailabilitySlot): CappedAvailabilitySlotView {
    return CappedAvailabilitySlotView(
        code = item.code,
        titleText = item.title,
        dockText = item.dock,
        durationMinutesText = item.durationMinutes.toString(),
        weight = "%.2f".format(item.weight),
        approved = "${item.approved}",
        archived = "${item.archived}",
        margin = "%.2f".format(item.margin),
        channelText = item.channel,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedAvailabilitySlotViews(items: MutableList<CappedAvailabilitySlot>): MutableList<CappedAvailabilitySlotView> {
    val views = mutableListOf<CappedAvailabilitySlotView>()
    for (item in items) {
        views.add(toCappedAvailabilitySlotView(item))
    }
    return views
}

fun sumCappedAvailabilitySlotView(view: CappedAvailabilitySlotView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    val parsedWeight = view.weight.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedDurationMinutes.toDouble() + parsedWeight + parsedMargin
}
