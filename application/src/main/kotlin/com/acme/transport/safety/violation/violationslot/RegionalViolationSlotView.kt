package com.acme.transport.safety.violation.violationslot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a regional violation slot. */
data class RegionalViolationSlotView(
    var codeText: String,
    var labelText: String,
    var startsAt: String,
    var ceiling: String,
    var channel: String,
    var revision: String,
    var statusLabel: String,
)

fun toRegionalViolationSlotView(item: RegionalViolationSlot): RegionalViolationSlotView {
    return RegionalViolationSlotView(
        codeText = item.code,
        labelText = item.label,
        startsAt = item.startsAt.toString(),
        ceiling = "%.2f".format(item.ceiling),
        channel = truncate(item.channel, 80),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalViolationSlotViews(items: MutableList<RegionalViolationSlot>): MutableList<RegionalViolationSlotView> {
    val views = mutableListOf<RegionalViolationSlotView>()
    for (item in items) {
        views.add(toRegionalViolationSlotView(item))
    }
    return views
}

fun sumRegionalViolationSlotView(view: RegionalViolationSlotView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedStartsAt.toDouble() + parsedCeiling + parsedRevision.toDouble()
}
