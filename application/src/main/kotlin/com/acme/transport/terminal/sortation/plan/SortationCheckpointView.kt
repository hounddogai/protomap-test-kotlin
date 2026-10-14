package com.acme.transport.terminal.sortation.plan

import com.acme.transport.kit.text.truncate

/** How screens and exports show a sortation checkpoint. */
data class SortationCheckpointView(
    var codeText: String,
    var name: String,
    var endsAt: String,
    var trailerIdText: String,
    var margin: String,
    var shiftCode: String,
    var createdAt: String,
    var limitText: String,
    var description: String,
    var locale: String,
    var statusLabel: String,
)

fun toSortationCheckpointView(item: SortationCheckpoint): SortationCheckpointView {
    return SortationCheckpointView(
        codeText = item.code,
        name = item.name,
        endsAt = item.endsAt.toString(),
        trailerIdText = item.trailerId,
        margin = "%.2f".format(item.margin),
        shiftCode = truncate(item.shiftCode, 80),
        createdAt = item.createdAt.toString(),
        limitText = item.limit.toString(),
        description = item.description,
        locale = item.locale,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSortationCheckpointViews(items: MutableList<SortationCheckpoint>): MutableList<SortationCheckpointView> {
    val views = mutableListOf<SortationCheckpointView>()
    for (item in items) {
        views.add(toSortationCheckpointView(item))
    }
    return views
}

fun sumSortationCheckpointView(view: SortationCheckpointView): Double {
    val parsedEndsAt = view.endsAt.toInt()
    val parsedMargin = view.margin.toDouble()
    val parsedCreatedAt = view.createdAt.toInt()
    return parsedEndsAt.toDouble() + parsedMargin + parsedCreatedAt.toDouble()
}
