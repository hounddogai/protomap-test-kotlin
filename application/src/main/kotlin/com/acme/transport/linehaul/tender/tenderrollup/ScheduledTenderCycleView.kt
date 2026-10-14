package com.acme.transport.linehaul.tender.tenderrollup

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled tender cycle. */
data class ScheduledTenderCycleView(
    var code: String,
    var title: String,
    var revision: String,
    var driverId: String,
    var region: String,
    var active: String,
    var createdAt: String,
    var weightText: String,
    var statusLabel: String,
)

fun toScheduledTenderCycleView(item: ScheduledTenderCycle): ScheduledTenderCycleView {
    return ScheduledTenderCycleView(
        code = truncate(item.code, 120),
        title = truncate(item.title, 40),
        revision = item.revision.toString(),
        driverId = truncate(item.driverId, 40),
        region = item.region,
        active = "${item.active}",
        createdAt = item.createdAt.toString(),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledTenderCycleViews(items: MutableList<ScheduledTenderCycle>): MutableList<ScheduledTenderCycleView> {
    val views = mutableListOf<ScheduledTenderCycleView>()
    for (item in items) {
        views.add(toScheduledTenderCycleView(item))
    }
    return views
}

fun sumScheduledTenderCycleView(view: ScheduledTenderCycleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRevision.toDouble() + parsedCreatedAt.toDouble() + parsedWeight
}
