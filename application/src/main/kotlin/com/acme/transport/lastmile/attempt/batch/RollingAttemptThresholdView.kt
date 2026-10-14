package com.acme.transport.lastmile.attempt.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling attempt threshold. */
data class RollingAttemptThresholdView(
    var code: String,
    var label: String,
    var carrierText: String,
    var notes: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toRollingAttemptThresholdView(item: RollingAttemptThreshold): RollingAttemptThresholdView {
    return RollingAttemptThresholdView(
        code = item.code,
        label = item.label,
        carrierText = truncate(item.carrier, 64),
        notes = item.notes,
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingAttemptThresholdViews(items: MutableList<RollingAttemptThreshold>): MutableList<RollingAttemptThresholdView> {
    val views = mutableListOf<RollingAttemptThresholdView>()
    for (item in items) {
        views.add(toRollingAttemptThresholdView(item))
    }
    return views
}

fun sumRollingAttemptThresholdView(view: RollingAttemptThresholdView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    return parsedBatchSize.toDouble()
}
