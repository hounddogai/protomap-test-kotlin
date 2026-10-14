package com.acme.transport.safety.incident.incidentbatch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a weighted incident batch. */
data class WeightedIncidentBatchView(
    var code: String,
    var labelText: String,
    var sequence: String,
    var pinned: String,
    var quantity: String,
    var margin: String,
    var statusLabel: String,
)

fun toWeightedIncidentBatchView(item: WeightedIncidentBatch): WeightedIncidentBatchView {
    return WeightedIncidentBatchView(
        code = truncate(item.code, 40),
        labelText = item.label,
        sequence = item.sequence.toString(),
        pinned = "${item.pinned}",
        quantity = item.quantity.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedIncidentBatchViews(items: MutableList<WeightedIncidentBatch>): MutableList<WeightedIncidentBatchView> {
    val views = mutableListOf<WeightedIncidentBatchView>()
    for (item in items) {
        views.add(toWeightedIncidentBatchView(item))
    }
    return views
}

fun sumWeightedIncidentBatchView(view: WeightedIncidentBatchView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedQuantity = view.quantity.toInt()
    val parsedMargin = view.margin.toDouble()
    return parsedSequence.toDouble() + parsedQuantity.toDouble() + parsedMargin
}
