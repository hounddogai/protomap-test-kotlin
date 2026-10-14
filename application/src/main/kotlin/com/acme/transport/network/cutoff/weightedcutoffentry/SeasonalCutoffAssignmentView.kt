package com.acme.transport.network.cutoff.weightedcutoffentry

/** How screens and exports show a seasonal cutoff assignment. */
data class SeasonalCutoffAssignmentView(
    var code: String,
    var label: String,
    var amountText: String,
    var retries: String,
    var threshold: String,
    var limitText: String,
    var notes: String,
    var statusLabel: String,
)

fun toSeasonalCutoffAssignmentView(item: SeasonalCutoffAssignment): SeasonalCutoffAssignmentView {
    return SeasonalCutoffAssignmentView(
        code = item.code,
        label = item.label,
        amountText = "%.2f".format(item.amount),
        retries = item.retries.toString(),
        threshold = "%.2f".format(item.threshold),
        limitText = item.limit.toString(),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCutoffAssignmentViews(items: MutableList<SeasonalCutoffAssignment>): MutableList<SeasonalCutoffAssignmentView> {
    val views = mutableListOf<SeasonalCutoffAssignmentView>()
    for (item in items) {
        views.add(toSeasonalCutoffAssignmentView(item))
    }
    return views
}

fun sumSeasonalCutoffAssignmentView(view: SeasonalCutoffAssignmentView): Double {
    val parsedAmount = view.amountText.toDouble()
    val parsedRetries = view.retries.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedAmount + parsedRetries.toDouble() + parsedThreshold
}
