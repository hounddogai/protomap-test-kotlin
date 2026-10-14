package com.acme.transport.terminal.traileryard.estimate

/** How screens and exports show a weighted trailer yard agreement. */
data class WeightedTrailerYardAgreementView(
    var code: String,
    var labelText: String,
    var rate: String,
    var locale: String,
    var notesText: String,
    var approved: String,
    var priority: String,
    var statusLabel: String,
)

fun toWeightedTrailerYardAgreementView(item: WeightedTrailerYardAgreement): WeightedTrailerYardAgreementView {
    return WeightedTrailerYardAgreementView(
        code = item.code,
        labelText = item.label,
        rate = "%.2f".format(item.rate),
        locale = item.locale,
        notesText = item.notes,
        approved = "${item.approved}",
        priority = item.priority.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toWeightedTrailerYardAgreementViews(items: MutableList<WeightedTrailerYardAgreement>): MutableList<WeightedTrailerYardAgreementView> {
    val views = mutableListOf<WeightedTrailerYardAgreementView>()
    for (item in items) {
        views.add(toWeightedTrailerYardAgreementView(item))
    }
    return views
}

fun sumWeightedTrailerYardAgreementView(view: WeightedTrailerYardAgreementView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedRate + parsedPriority.toDouble()
}
