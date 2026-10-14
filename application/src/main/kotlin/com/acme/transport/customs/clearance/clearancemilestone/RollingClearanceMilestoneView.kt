package com.acme.transport.customs.clearance.clearancemilestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling clearance milestone. */
data class RollingClearanceMilestoneView(
    var codeText: String,
    var label: String,
    var batchSizeText: String,
    var rate: String,
    var loadKg: String,
    var score: String,
    var statusLabel: String,
)

fun toRollingClearanceMilestoneView(item: RollingClearanceMilestone): RollingClearanceMilestoneView {
    return RollingClearanceMilestoneView(
        codeText = truncate(item.code, 80),
        label = item.label,
        batchSizeText = item.batchSize.toString(),
        rate = "%.2f".format(item.rate),
        loadKg = "%.2f".format(item.loadKg),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingClearanceMilestoneViews(items: MutableList<RollingClearanceMilestone>): MutableList<RollingClearanceMilestoneView> {
    val views = mutableListOf<RollingClearanceMilestoneView>()
    for (item in items) {
        views.add(toRollingClearanceMilestoneView(item))
    }
    return views
}

fun sumRollingClearanceMilestoneView(view: RollingClearanceMilestoneView): Double {
    val parsedBatchSize = view.batchSizeText.toInt()
    val parsedRate = view.rate.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedBatchSize.toDouble() + parsedRate + parsedLoadKg
}
