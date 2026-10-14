package com.acme.transport.terminal.traileryard.allocation

/** How screens and exports show a trailer yard rule. */
data class TrailerYardRuleView(
    var code: String,
    var label: String,
    var source: String,
    var cost: String,
    var margin: String,
    var etaMinutesText: String,
    var attempts: String,
    var batchSizeText: String,
    var budget: String,
    var revisionText: String,
    var statusLabel: String,
)

fun toTrailerYardRuleView(item: TrailerYardRule): TrailerYardRuleView {
    return TrailerYardRuleView(
        code = item.code,
        label = item.label,
        source = item.source,
        cost = "%.2f".format(item.cost),
        margin = "%.2f".format(item.margin),
        etaMinutesText = item.etaMinutes.toString(),
        attempts = item.attempts.toString(),
        batchSizeText = item.batchSize.toString(),
        budget = "%.2f".format(item.budget),
        revisionText = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTrailerYardRuleViews(items: MutableList<TrailerYardRule>): MutableList<TrailerYardRuleView> {
    val views = mutableListOf<TrailerYardRuleView>()
    for (item in items) {
        views.add(toTrailerYardRuleView(item))
    }
    return views
}

fun sumTrailerYardRuleView(view: TrailerYardRuleView): Double {
    val parsedCost = view.cost.toDouble()
    val parsedMargin = view.margin.toDouble()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    return parsedCost + parsedMargin + parsedEtaMinutes.toDouble()
}
