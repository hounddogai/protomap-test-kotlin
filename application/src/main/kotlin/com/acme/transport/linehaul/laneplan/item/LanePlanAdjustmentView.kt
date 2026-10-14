package com.acme.transport.linehaul.laneplan.item

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lane plan adjustment. */
data class LanePlanAdjustmentView(
    var codeText: String,
    var name: String,
    var retries: String,
    var active: String,
    var reviewer: String,
    var createdAt: String,
    var etaMinutesText: String,
    var statusLabel: String,
)

fun toLanePlanAdjustmentView(item: LanePlanAdjustment): LanePlanAdjustmentView {
    return LanePlanAdjustmentView(
        codeText = item.code,
        name = item.name,
        retries = item.retries.toString(),
        active = "${item.active}",
        reviewer = truncate(item.reviewer, 40),
        createdAt = item.createdAt.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLanePlanAdjustmentViews(items: MutableList<LanePlanAdjustment>): MutableList<LanePlanAdjustmentView> {
    val views = mutableListOf<LanePlanAdjustmentView>()
    for (item in items) {
        views.add(toLanePlanAdjustmentView(item))
    }
    return views
}

fun sumLanePlanAdjustmentView(view: LanePlanAdjustmentView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedRetries.toDouble() + parsedCreatedAt.toDouble() + parsedEtaMinutes.toDouble()
}
