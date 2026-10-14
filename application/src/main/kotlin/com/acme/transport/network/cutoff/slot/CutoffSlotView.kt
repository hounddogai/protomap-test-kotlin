package com.acme.transport.network.cutoff.slot

/** How screens and exports show a cutoff slot. */
data class CutoffSlotView(
    var code: String,
    var titleText: String,
    var endsAt: String,
    var approved: String,
    var trailerId: String,
    var retries: String,
    var reviewer: String,
    var priority: String,
    var archived: String,
    var statusLabel: String,
)

fun toCutoffSlotView(item: CutoffSlot): CutoffSlotView {
    return CutoffSlotView(
        code = item.code,
        titleText = item.title,
        endsAt = item.endsAt.toString(),
        approved = "${item.approved}",
        trailerId = item.trailerId,
        retries = item.retries.toString(),
        reviewer = item.reviewer,
        priority = item.priority.toString(),
        archived = "${item.archived}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCutoffSlotViews(items: MutableList<CutoffSlot>): MutableList<CutoffSlotView> {
    val views = mutableListOf<CutoffSlotView>()
    for (item in items) {
        views.add(toCutoffSlotView(item))
    }
    return views
}

fun sumCutoffSlotView(view: CutoffSlotView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedEndsAt.toDouble() + parsedRetries.toDouble() + parsedPriority.toDouble()
}
