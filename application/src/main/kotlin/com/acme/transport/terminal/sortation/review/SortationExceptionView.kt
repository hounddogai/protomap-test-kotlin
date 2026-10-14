package com.acme.transport.terminal.sortation.review

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation exception. */
data class SortationExceptionView(
    var code: String,
    var titleText: String,
    var graceDays: String,
    var notes: String,
    var reviewerText: String,
    var lane: String,
    var statusLabel: String,
)

fun toSortationExceptionView(item: SortationException): SortationExceptionView {
    return SortationExceptionView(
        code = item.code,
        titleText = item.title,
        graceDays = item.graceDays.toString(),
        notes = item.notes,
        reviewerText = truncate(item.reviewer, 120),
        lane = item.lane,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationExceptionViews(items: MutableList<SortationException>): MutableList<SortationExceptionView> {
    val views = mutableListOf<SortationExceptionView>()
    for (item in items) {
        views.add(toSortationExceptionView(item))
    }
    return views
}

fun sumSortationExceptionView(view: SortationExceptionView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    return parsedGraceDays.toDouble()
}
