package com.acme.transport.lastmile.drop.dropslot

/** How screens and exports show a seasonal drop agreement. */
data class SeasonalDropAgreementView(
    var codeText: String,
    var label: String,
    var expiresAt: String,
    var archived: String,
    var thresholdText: String,
    var nameText: String,
    var margin: String,
    var budget: String,
    var weight: String,
    var statusLabel: String,
)

fun toSeasonalDropAgreementView(item: SeasonalDropAgreement): SeasonalDropAgreementView {
    return SeasonalDropAgreementView(
        codeText = item.code,
        label = item.label,
        expiresAt = item.expiresAt.toString(),
        archived = "${item.archived}",
        thresholdText = "%.2f".format(item.threshold),
        nameText = item.name,
        margin = "%.2f".format(item.margin),
        budget = "%.2f".format(item.budget),
        weight = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalDropAgreementViews(items: MutableList<SeasonalDropAgreement>): MutableList<SeasonalDropAgreementView> {
    val views = mutableListOf<SeasonalDropAgreementView>()
    for (item in items) {
        views.add(toSeasonalDropAgreementView(item))
    }
    return views
}

fun sumSeasonalDropAgreementView(view: SeasonalDropAgreementView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedMargin = view.margin.toDouble()
    return parsedExpiresAt.toDouble() + parsedThreshold + parsedMargin
}
