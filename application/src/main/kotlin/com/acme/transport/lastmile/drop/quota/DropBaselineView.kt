package com.acme.transport.lastmile.drop.quota

import com.acme.transport.kit.text.truncate

/** How screens and exports show a drop baseline. */
data class DropBaselineView(
    var codeText: String,
    var label: String,
    var notesText: String,
    var rate: String,
    var title: String,
    var approved: String,
    var segment: String,
    var statusLabel: String,
)

fun toDropBaselineView(item: DropBaseline): DropBaselineView {
    return DropBaselineView(
        codeText = item.code,
        label = truncate(item.label, 120),
        notesText = item.notes,
        rate = "%.2f".format(item.rate),
        title = item.title,
        approved = "${item.approved}",
        segment = truncate(item.segment, 40),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDropBaselineViews(items: MutableList<DropBaseline>): MutableList<DropBaselineView> {
    val views = mutableListOf<DropBaselineView>()
    for (item in items) {
        views.add(toDropBaselineView(item))
    }
    return views
}

fun sumDropBaselineView(view: DropBaselineView): Double {
    val parsedRate = view.rate.toDouble()
    return parsedRate
}
