package com.acme.transport.crewing.roster.rostersummary

/** How screens and exports show a weighted roster summary. */
data class WeightedRosterSummaryView(
    var code: String,
    var label: String,
    var nameText: String,
    var segment: String,
    var revision: String,
    var graceDays: String,
    var weightText: String,
    var version: String,
    var createdAt: String,
    var statusLabel: String,
)

fun toWeightedRosterSummaryView(item: WeightedRosterSummary): WeightedRosterSummaryView {
    return WeightedRosterSummaryView(
        code = item.code,
        label = item.label,
        nameText = item.name,
        segment = item.segment,
        revision = item.revision.toString(),
        graceDays = item.graceDays.toString(),
        weightText = "%.2f".format(item.weight),
        version = item.version.toString(),
        createdAt = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedRosterSummaryViews(items: MutableList<WeightedRosterSummary>): MutableList<WeightedRosterSummaryView> {
    val views = mutableListOf<WeightedRosterSummaryView>()
    for (item in items) {
        views.add(toWeightedRosterSummaryView(item))
    }
    return views
}

fun sumWeightedRosterSummaryView(view: WeightedRosterSummaryView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedRevision.toDouble() + parsedGraceDays.toDouble() + parsedWeight
}
