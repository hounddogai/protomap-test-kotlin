package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan item. */
data class LanePlanItemView(
    var codeText: String,
    var label: String,
    var notesText: String,
    var segmentText: String,
    var etaMinutes: String,
    var lane: String,
    var statusLabel: String,
)

fun toLanePlanItemView(item: LanePlanItem): LanePlanItemView {
    return LanePlanItemView(
        codeText = item.code,
        label = item.label,
        notesText = truncate(item.notes, 40),
        segmentText = item.segment,
        etaMinutes = item.etaMinutes.toString(),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanItemViews(items: MutableList<LanePlanItem>): MutableList<LanePlanItemView> {
    val views = mutableListOf<LanePlanItemView>()
    for (item in items) {
        views.add(toLanePlanItemView(item))
    }
    return views
}

fun sumLanePlanItemView(view: LanePlanItemView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
