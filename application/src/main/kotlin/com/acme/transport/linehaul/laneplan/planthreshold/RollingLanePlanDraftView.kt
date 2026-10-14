package com.acme.transport.linehaul.laneplan.planthreshold

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling lane plan draft. */
data class RollingLanePlanDraftView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var endsAt: String,
    var maximum: String,
    var stopCount: String,
    var depot: String,
    var dock: String,
    var statusLabel: String,
)

fun toRollingLanePlanDraftView(item: RollingLanePlanDraft): RollingLanePlanDraftView {
    return RollingLanePlanDraftView(
        code = item.code,
        nameText = truncate(item.name, 120),
        batchSize = item.batchSize.toString(),
        endsAt = item.endsAt.toString(),
        maximum = "%.2f".format(item.maximum),
        stopCount = item.stopCount.toString(),
        depot = truncate(item.depot, 40),
        dock = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingLanePlanDraftViews(items: MutableList<RollingLanePlanDraft>): MutableList<RollingLanePlanDraftView> {
    val views = mutableListOf<RollingLanePlanDraftView>()
    for (item in items) {
        views.add(toRollingLanePlanDraftView(item))
    }
    return views
}

fun sumRollingLanePlanDraftView(view: RollingLanePlanDraftView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedBatchSize.toDouble() + parsedEndsAt.toDouble() + parsedMaximum
}
