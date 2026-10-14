package com.acme.transport.emissions.idling.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a idling assignment. */
data class IdlingAssignmentView(
    var codeText: String,
    var labelText: String,
    var updatedAt: String,
    var localeText: String,
    var visible: String,
    var segment: String,
    var pinned: String,
    var stopCountText: String,
    var minimumText: String,
    var reviewer: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toIdlingAssignmentView(item: IdlingAssignment): IdlingAssignmentView {
    return IdlingAssignmentView(
        codeText = truncate(item.code, 80),
        labelText = item.label,
        updatedAt = item.updatedAt.toString(),
        localeText = item.locale,
        visible = "${item.visible}",
        segment = item.segment,
        pinned = "${item.pinned}",
        stopCountText = item.stopCount.toString(),
        minimumText = "%.2f".format(item.minimum),
        reviewer = item.reviewer,
        phoneText = truncate(item.phone, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingAssignmentViews(items: MutableList<IdlingAssignment>): MutableList<IdlingAssignmentView> {
    val views = mutableListOf<IdlingAssignmentView>()
    for (item in items) {
        views.add(toIdlingAssignmentView(item))
    }
    return views
}

fun sumIdlingAssignmentView(view: IdlingAssignmentView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedStopCount = view.stopCountText.toInt()
    val parsedMinimum = view.minimumText.toDouble()
    return parsedUpdatedAt.toDouble() + parsedStopCount.toDouble() + parsedMinimum
}
