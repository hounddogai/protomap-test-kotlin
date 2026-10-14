package com.acme.transport.terminal.dwell.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient dwell milestone. */
data class LenientDwellMilestoneView(
    var codeText: String,
    var label: String,
    var minimumText: String,
    var trailerIdText: String,
    var retries: String,
    var statusLabel: String,
)

fun toLenientDwellMilestoneView(item: LenientDwellMilestone): LenientDwellMilestoneView {
    return LenientDwellMilestoneView(
        codeText = truncate(item.code, 64),
        label = item.label,
        minimumText = "%.2f".format(item.minimum),
        trailerIdText = truncate(item.trailerId, 120),
        retries = item.retries.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientDwellMilestoneViews(items: MutableList<LenientDwellMilestone>): MutableList<LenientDwellMilestoneView> {
    val views = mutableListOf<LenientDwellMilestoneView>()
    for (item in items) {
        views.add(toLenientDwellMilestoneView(item))
    }
    return views
}

fun sumLenientDwellMilestoneView(view: LenientDwellMilestoneView): Double {
    val parsedMinimum = view.minimumText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedMinimum + parsedRetries.toDouble()
}
