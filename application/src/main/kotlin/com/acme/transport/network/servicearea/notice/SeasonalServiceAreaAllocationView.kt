package com.acme.transport.network.servicearea.notice

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal service area allocation. */
data class SeasonalServiceAreaAllocationView(
    var code: String,
    var title: String,
    var description: String,
    var label: String,
    var summary: String,
    var sequenceText: String,
    var startsAt: String,
    var limit: String,
    var statusLabel: String,
)

fun toSeasonalServiceAreaAllocationView(item: SeasonalServiceAreaAllocation): SeasonalServiceAreaAllocationView {
    return SeasonalServiceAreaAllocationView(
        code = item.code,
        title = truncate(item.title, 40),
        description = item.description,
        label = item.label,
        summary = truncate(item.summary, 64),
        sequenceText = item.sequence.toString(),
        startsAt = item.startsAt.toString(),
        limit = item.limit.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalServiceAreaAllocationViews(items: MutableList<SeasonalServiceAreaAllocation>): MutableList<SeasonalServiceAreaAllocationView> {
    val views = mutableListOf<SeasonalServiceAreaAllocationView>()
    for (item in items) {
        views.add(toSeasonalServiceAreaAllocationView(item))
    }
    return views
}

fun sumSeasonalServiceAreaAllocationView(view: SeasonalServiceAreaAllocationView): Double {
    val parsedSequence = view.sequenceText.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    val parsedLimit = view.limit.toInt()
    return parsedSequence.toDouble() + parsedStartsAt.toDouble() + parsedLimit.toDouble()
}
