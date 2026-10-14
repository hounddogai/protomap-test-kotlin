package com.acme.transport.terminal.traileryard.allocation

/** How screens and exports show a trailer yard allocation. */
data class TrailerYardAllocationView(
    var codeText: String,
    var label: String,
    var archived: String,
    var segment: String,
    var attempts: String,
    var durationMinutes: String,
    var priority: String,
    var graceDays: String,
    var statusLabel: String,
)

fun toTrailerYardAllocationView(item: TrailerYardAllocation): TrailerYardAllocationView {
    return TrailerYardAllocationView(
        codeText = item.code,
        label = item.label,
        archived = "${item.archived}",
        segment = item.segment,
        attempts = item.attempts.toString(),
        durationMinutes = item.durationMinutes.toString(),
        priority = item.priority.toString(),
        graceDays = item.graceDays.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardAllocationViews(items: MutableList<TrailerYardAllocation>): MutableList<TrailerYardAllocationView> {
    val views = mutableListOf<TrailerYardAllocationView>()
    for (item in items) {
        views.add(toTrailerYardAllocationView(item))
    }
    return views
}

fun sumTrailerYardAllocationView(view: TrailerYardAllocationView): Double {
    val parsedAttempts = view.attempts.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedAttempts.toDouble() + parsedDurationMinutes.toDouble() + parsedPriority.toDouble()
}
