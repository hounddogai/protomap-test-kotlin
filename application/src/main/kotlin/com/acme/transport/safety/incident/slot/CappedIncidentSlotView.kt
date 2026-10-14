package com.acme.transport.safety.incident.slot

/** How screens and exports show a capped incident slot. */
data class CappedIncidentSlotView(
    var code: String,
    var titleText: String,
    var marginText: String,
    var contactEmailText: String,
    var capacity: String,
    var attempts: String,
    var stopCount: String,
    var score: String,
    var statusLabel: String,
)

fun toCappedIncidentSlotView(item: CappedIncidentSlot): CappedIncidentSlotView {
    return CappedIncidentSlotView(
        code = item.code,
        titleText = item.title,
        marginText = "%.2f".format(item.margin),
        contactEmailText = item.contactEmail,
        capacity = item.capacity.toString(),
        attempts = item.attempts.toString(),
        stopCount = item.stopCount.toString(),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCappedIncidentSlotViews(items: MutableList<CappedIncidentSlot>): MutableList<CappedIncidentSlotView> {
    val views = mutableListOf<CappedIncidentSlotView>()
    for (item in items) {
        views.add(toCappedIncidentSlotView(item))
    }
    return views
}

fun sumCappedIncidentSlotView(view: CappedIncidentSlotView): Double {
    val parsedMargin = view.marginText.toDouble()
    val parsedCapacity = view.capacity.toInt()
    val parsedAttempts = view.attempts.toInt()
    return parsedMargin + parsedCapacity.toDouble() + parsedAttempts.toDouble()
}
