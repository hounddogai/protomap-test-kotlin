package com.acme.transport.freightpay.audit.cycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual audit override. */
data class ManualAuditOverrideView(
    var codeText: String,
    var nameText: String,
    var minimum: String,
    var sequence: String,
    var visibleText: String,
    var startsAt: String,
    var externalRef: String,
    var graceDays: String,
    var margin: String,
    var statusLabel: String,
)

fun toManualAuditOverrideView(item: ManualAuditOverride): ManualAuditOverrideView {
    return ManualAuditOverrideView(
        codeText = truncate(item.code, 120),
        nameText = item.name,
        minimum = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        visibleText = "${item.visible}",
        startsAt = item.startsAt.toString(),
        externalRef = item.externalRef,
        graceDays = item.graceDays.toString(),
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualAuditOverrideViews(items: MutableList<ManualAuditOverride>): MutableList<ManualAuditOverrideView> {
    val views = mutableListOf<ManualAuditOverrideView>()
    for (item in items) {
        views.add(toManualAuditOverrideView(item))
    }
    return views
}

fun sumManualAuditOverrideView(view: ManualAuditOverrideView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMinimum + parsedSequence.toDouble() + parsedStartsAt.toDouble()
}
