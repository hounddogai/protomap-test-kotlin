package com.acme.transport.customs.inspection.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending inspection baseline. */
data class PendingInspectionBaselineView(
    var codeText: String,
    var title: String,
    var externalRef: String,
    var retriesText: String,
    var ratio: String,
    var shiftCode: String,
    var capacity: String,
    var description: String,
    var endsAt: String,
    var locale: String,
    var statusLabel: String,
)

fun toPendingInspectionBaselineView(item: PendingInspectionBaseline): PendingInspectionBaselineView {
    return PendingInspectionBaselineView(
        codeText = item.code,
        title = item.title,
        externalRef = truncate(item.externalRef, 80),
        retriesText = item.retries.toString(),
        ratio = "%.2f".format(item.ratio),
        shiftCode = item.shiftCode,
        capacity = item.capacity.toString(),
        description = item.description,
        endsAt = item.endsAt.toString(),
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingInspectionBaselineViews(items: MutableList<PendingInspectionBaseline>): MutableList<PendingInspectionBaselineView> {
    val views = mutableListOf<PendingInspectionBaselineView>()
    for (item in items) {
        views.add(toPendingInspectionBaselineView(item))
    }
    return views
}

fun sumPendingInspectionBaselineView(view: PendingInspectionBaselineView): Double {
    val parsedRetries = view.retriesText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedCapacity = view.capacity.toInt()
    return parsedRetries.toDouble() + parsedRatio + parsedCapacity.toDouble()
}
