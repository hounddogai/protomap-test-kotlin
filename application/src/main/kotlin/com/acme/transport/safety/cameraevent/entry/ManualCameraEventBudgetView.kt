package com.acme.transport.safety.cameraevent.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual camera event budget. */
data class ManualCameraEventBudgetView(
    var code: String,
    var title: String,
    var notes: String,
    var depotText: String,
    var dock: String,
    var sequence: String,
    var retries: String,
    var threshold: String,
    var ceiling: String,
    var unit: String,
    var maximum: String,
    var statusLabel: String,
)

fun toManualCameraEventBudgetView(item: ManualCameraEventBudget): ManualCameraEventBudgetView {
    return ManualCameraEventBudgetView(
        code = truncate(item.code, 64),
        title = item.title,
        notes = item.notes,
        depotText = item.depot,
        dock = item.dock,
        sequence = item.sequence.toString(),
        retries = item.retries.toString(),
        threshold = "%.2f".format(item.threshold),
        ceiling = "%.2f".format(item.ceiling),
        unit = item.unit,
        maximum = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualCameraEventBudgetViews(items: MutableList<ManualCameraEventBudget>): MutableList<ManualCameraEventBudgetView> {
    val views = mutableListOf<ManualCameraEventBudgetView>()
    for (item in items) {
        views.add(toManualCameraEventBudgetView(item))
    }
    return views
}

fun sumManualCameraEventBudgetView(view: ManualCameraEventBudgetView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedSequence.toDouble() + parsedRetries.toDouble() + parsedThreshold
}
