package com.acme.transport.terminal.dwell.score

import com.acme.transport.kit.text.truncate

/** How screens and exports show a dwell rollup. */
data class DwellRollupView(
    var code: String,
    var title: String,
    var timezone: String,
    var createdAt: String,
    var updatedAt: String,
    var summaryText: String,
    var threshold: String,
    var dock: String,
    var batchSize: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toDwellRollupView(item: DwellRollup): DwellRollupView {
    return DwellRollupView(
        code = truncate(item.code, 40),
        title = item.title,
        timezone = truncate(item.timezone, 40),
        createdAt = item.createdAt.toString(),
        updatedAt = item.updatedAt.toString(),
        summaryText = item.summary,
        threshold = "%.2f".format(item.threshold),
        dock = item.dock,
        batchSize = item.batchSize.toString(),
        contactEmail = truncate(item.contactEmail, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDwellRollupViews(items: MutableList<DwellRollup>): MutableList<DwellRollupView> {
    val views = mutableListOf<DwellRollupView>()
    for (item in items) {
        views.add(toDwellRollupView(item))
    }
    return views
}

fun sumDwellRollupView(view: DwellRollupView): Double {
    val parsedCreatedAt = view.createdAt.toInt()
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedCreatedAt.toDouble() + parsedUpdatedAt.toDouble() + parsedThreshold
}
