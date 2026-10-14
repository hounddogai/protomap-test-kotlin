package com.acme.transport.network.transittime.rollup

/** How screens and exports show a legacy transit time rollup. */
data class LegacyTransitTimeRollupView(
    var code: String,
    var labelText: String,
    var minimum: String,
    var sequence: String,
    var startsAt: String,
    var statusLabel: String,
)

fun toLegacyTransitTimeRollupView(item: LegacyTransitTimeRollup): LegacyTransitTimeRollupView {
    return LegacyTransitTimeRollupView(
        code = item.code,
        labelText = item.label,
        minimum = "%.2f".format(item.minimum),
        sequence = item.sequence.toString(),
        startsAt = item.startsAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyTransitTimeRollupViews(items: MutableList<LegacyTransitTimeRollup>): MutableList<LegacyTransitTimeRollupView> {
    val views = mutableListOf<LegacyTransitTimeRollupView>()
    for (item in items) {
        views.add(toLegacyTransitTimeRollupView(item))
    }
    return views
}

fun sumLegacyTransitTimeRollupView(view: LegacyTransitTimeRollupView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedSequence = view.sequence.toInt()
    val parsedStartsAt = view.startsAt.toInt()
    return parsedMinimum + parsedSequence.toDouble() + parsedStartsAt.toDouble()
}
