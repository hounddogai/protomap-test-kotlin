package com.acme.transport.safety.incident.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident batch. */
data class IncidentBatchView(
    var code: String,
    var labelText: String,
    var archivedText: String,
    var limitText: String,
    var trailerId: String,
    var description: String,
    var statusLabel: String,
)

fun toIncidentBatchView(item: IncidentBatch): IncidentBatchView {
    return IncidentBatchView(
        code = item.code,
        labelText = item.label,
        archivedText = "${item.archived}",
        limitText = item.limit.toString(),
        trailerId = item.trailerId,
        description = truncate(item.description, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentBatchViews(items: MutableList<IncidentBatch>): MutableList<IncidentBatchView> {
    val views = mutableListOf<IncidentBatchView>()
    for (item in items) {
        views.add(toIncidentBatchView(item))
    }
    return views
}

fun sumIncidentBatchView(view: IncidentBatchView): Double {
    val parsedLimit = view.limitText.toInt()
    return parsedLimit.toDouble()
}
