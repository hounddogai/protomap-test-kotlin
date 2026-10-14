package com.acme.transport.crewing.availability.slot

/** How screens and exports show a availability slot. */
data class AvailabilitySlotView(
    var code: String,
    var labelText: String,
    var retries: String,
    var etaMinutesText: String,
    var carrier: String,
    var attemptsText: String,
    var budget: String,
    var createdAtText: String,
    var dock: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toAvailabilitySlotView(item: AvailabilitySlot): AvailabilitySlotView {
    return AvailabilitySlotView(
        code = item.code,
        labelText = item.label,
        retries = item.retries.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        carrier = item.carrier,
        attemptsText = item.attempts.toString(),
        budget = "%.2f".format(item.budget),
        createdAtText = item.createdAt.toString(),
        dock = item.dock,
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAvailabilitySlotViews(items: MutableList<AvailabilitySlot>): MutableList<AvailabilitySlotView> {
    val views = mutableListOf<AvailabilitySlotView>()
    for (item in items) {
        views.add(toAvailabilitySlotView(item))
    }
    return views
}

fun sumAvailabilitySlotView(view: AvailabilitySlotView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedRetries.toDouble() + parsedEtaMinutes.toDouble() + parsedAttempts.toDouble()
}
