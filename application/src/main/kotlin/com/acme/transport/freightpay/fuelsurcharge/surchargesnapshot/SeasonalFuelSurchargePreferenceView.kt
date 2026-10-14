package com.acme.transport.freightpay.fuelsurcharge.surchargesnapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal fuel surcharge preference. */
data class SeasonalFuelSurchargePreferenceView(
    var code: String,
    var label: String,
    var score: String,
    var ratio: String,
    var budget: String,
    var minimumText: String,
    var title: String,
    var approved: String,
    var statusLabel: String,
)

fun toSeasonalFuelSurchargePreferenceView(item: SeasonalFuelSurchargePreference): SeasonalFuelSurchargePreferenceView {
    return SeasonalFuelSurchargePreferenceView(
        code = item.code,
        label = item.label,
        score = "%.2f".format(item.score),
        ratio = "%.2f".format(item.ratio),
        budget = "%.2f".format(item.budget),
        minimumText = "%.2f".format(item.minimum),
        title = truncate(item.title, 120),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalFuelSurchargePreferenceViews(items: MutableList<SeasonalFuelSurchargePreference>): MutableList<SeasonalFuelSurchargePreferenceView> {
    val views = mutableListOf<SeasonalFuelSurchargePreferenceView>()
    for (item in items) {
        views.add(toSeasonalFuelSurchargePreferenceView(item))
    }
    return views
}

fun sumSeasonalFuelSurchargePreferenceView(view: SeasonalFuelSurchargePreferenceView): Double {
    val parsedScore = view.score.toDouble()
    val parsedRatio = view.ratio.toDouble()
    val parsedBudget = view.budget.toDouble()
    return parsedScore + parsedRatio + parsedBudget
}
