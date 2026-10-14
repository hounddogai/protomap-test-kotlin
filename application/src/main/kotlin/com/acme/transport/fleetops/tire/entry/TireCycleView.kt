package com.acme.transport.fleetops.tire.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire cycle. */
data class TireCycleView(
    var code: String,
    var nameText: String,
    var contactEmail: String,
    var updatedAtText: String,
    var description: String,
    var stopCount: String,
    var summaryText: String,
    var priorityText: String,
    var reviewer: String,
    var activeText: String,
    var statusLabel: String,
)

fun toTireCycleView(item: TireCycle): TireCycleView {
    return TireCycleView(
        code = item.code,
        nameText = item.name,
        contactEmail = truncate(item.contactEmail, 80),
        updatedAtText = item.updatedAt.toString(),
        description = item.description,
        stopCount = item.stopCount.toString(),
        summaryText = item.summary,
        priorityText = item.priority.toString(),
        reviewer = item.reviewer,
        activeText = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireCycleViews(items: MutableList<TireCycle>): MutableList<TireCycleView> {
    val views = mutableListOf<TireCycleView>()
    for (item in items) {
        views.add(toTireCycleView(item))
    }
    return views
}

fun sumTireCycleView(view: TireCycleView): Double {
    val parsedUpdatedAt = view.updatedAtText.toInt()
    val parsedStopCount = view.stopCount.toInt()
    val parsedPriority = view.priorityText.toInt()
    return parsedUpdatedAt.toDouble() + parsedStopCount.toDouble() + parsedPriority.toDouble()
}
