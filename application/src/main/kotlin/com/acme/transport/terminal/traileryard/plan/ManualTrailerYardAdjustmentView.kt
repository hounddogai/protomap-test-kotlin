package com.acme.transport.terminal.traileryard.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual trailer yard adjustment. */
data class ManualTrailerYardAdjustmentView(
    var code: String,
    var nameText: String,
    var startsAt: String,
    var trailerId: String,
    var revision: String,
    var statusLabel: String,
)

fun toManualTrailerYardAdjustmentView(item: ManualTrailerYardAdjustment): ManualTrailerYardAdjustmentView {
    return ManualTrailerYardAdjustmentView(
        code = item.code,
        nameText = item.name,
        startsAt = item.startsAt.toString(),
        trailerId = truncate(item.trailerId, 40),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTrailerYardAdjustmentViews(items: MutableList<ManualTrailerYardAdjustment>): MutableList<ManualTrailerYardAdjustmentView> {
    val views = mutableListOf<ManualTrailerYardAdjustmentView>()
    for (item in items) {
        views.add(toManualTrailerYardAdjustmentView(item))
    }
    return views
}

fun sumManualTrailerYardAdjustmentView(view: ManualTrailerYardAdjustmentView): Double {
    val parsedStartsAt = view.startsAt.toInt()
    val parsedRevision = view.revision.toInt()
    return parsedStartsAt.toDouble() + parsedRevision.toDouble()
}
