package com.acme.transport.emissions.emissionfactor.template

/** How screens and exports show a legacy emission factor preference. */
data class LegacyEmissionFactorPreferenceView(
    var code: String,
    var title: String,
    var rate: String,
    var minimum: String,
    var name: String,
    var thresholdText: String,
    var startsAtText: String,
    var score: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLegacyEmissionFactorPreferenceView(item: LegacyEmissionFactorPreference): LegacyEmissionFactorPreferenceView {
    return LegacyEmissionFactorPreferenceView(
        code = item.code,
        title = item.title,
        rate = "%.2f".format(item.rate),
        minimum = "%.2f".format(item.minimum),
        name = item.name,
        thresholdText = "%.2f".format(item.threshold),
        startsAtText = item.startsAt.toString(),
        score = "%.2f".format(item.score),
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyEmissionFactorPreferenceViews(items: MutableList<LegacyEmissionFactorPreference>): MutableList<LegacyEmissionFactorPreferenceView> {
    val views = mutableListOf<LegacyEmissionFactorPreferenceView>()
    for (item in items) {
        views.add(toLegacyEmissionFactorPreferenceView(item))
    }
    return views
}

fun sumLegacyEmissionFactorPreferenceView(view: LegacyEmissionFactorPreferenceView): Double {
    val parsedRate = view.rate.toDouble()
    val parsedMinimum = view.minimum.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedRate + parsedMinimum + parsedThreshold
}
