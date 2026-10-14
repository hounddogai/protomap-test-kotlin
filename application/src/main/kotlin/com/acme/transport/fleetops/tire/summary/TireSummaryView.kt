package com.acme.transport.fleetops.tire.summary

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tire summary. */
data class TireSummaryView(
    var code: String,
    var title: String,
    var channel: String,
    var sequence: String,
    var revision: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toTireSummaryView(item: TireSummary): TireSummaryView {
    return TireSummaryView(
        code = item.code,
        title = item.title,
        channel = truncate(item.channel, 40),
        sequence = item.sequence.toString(),
        revision = item.revision.toString(),
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTireSummaryViews(items: MutableList<TireSummary>): MutableList<TireSummaryView> {
    val views = mutableListOf<TireSummaryView>()
    for (item in items) {
        views.add(toTireSummaryView(item))
    }
    return views
}

fun sumTireSummaryView(view: TireSummaryView): Double {
    val parsedSequence = view.sequence.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedSequence.toDouble() + parsedRevision.toDouble() + parsedDistanceKm
}
