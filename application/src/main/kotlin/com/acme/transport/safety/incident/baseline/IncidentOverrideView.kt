package com.acme.transport.safety.incident.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a incident override. */
data class IncidentOverrideView(
    var code: String,
    var title: String,
    var notes: String,
    var shiftCode: String,
    var amount: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toIncidentOverrideView(item: IncidentOverride): IncidentOverrideView {
    return IncidentOverrideView(
        code = item.code,
        title = truncate(item.title, 80),
        notes = item.notes,
        shiftCode = item.shiftCode,
        amount = "%.2f".format(item.amount),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIncidentOverrideViews(items: MutableList<IncidentOverride>): MutableList<IncidentOverrideView> {
    val views = mutableListOf<IncidentOverrideView>()
    for (item in items) {
        views.add(toIncidentOverrideView(item))
    }
    return views
}

fun sumIncidentOverrideView(view: IncidentOverrideView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedAmount + parsedBatchSize.toDouble()
}
