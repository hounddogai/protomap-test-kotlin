package com.acme.transport.network.servicearea.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a pending service area assignment. */
data class PendingServiceAreaAssignmentView(
    var code: String,
    var title: String,
    var summaryText: String,
    var phoneText: String,
    var minimumText: String,
    var statusLabel: String,
)

fun toPendingServiceAreaAssignmentView(item: PendingServiceAreaAssignment): PendingServiceAreaAssignmentView {
    return PendingServiceAreaAssignmentView(
        code = truncate(item.code, 64),
        title = item.title,
        summaryText = item.summary,
        phoneText = truncate(item.phone, 80),
        minimumText = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPendingServiceAreaAssignmentViews(items: MutableList<PendingServiceAreaAssignment>): MutableList<PendingServiceAreaAssignmentView> {
    val views = mutableListOf<PendingServiceAreaAssignmentView>()
    for (item in items) {
        views.add(toPendingServiceAreaAssignmentView(item))
    }
    return views
}

fun sumPendingServiceAreaAssignmentView(view: PendingServiceAreaAssignmentView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    return parsedMinimum
}
