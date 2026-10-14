package com.acme.transport.safety.coaching.coachingsegment

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal coaching variance. */
data class SeasonalCoachingVarianceView(
    var code: String,
    var label: String,
    var contactEmail: String,
    var timezone: String,
    var ratioText: String,
    var threshold: String,
    var ownerIdText: String,
    var summaryText: String,
    var statusLabel: String,
)

fun toSeasonalCoachingVarianceView(item: SeasonalCoachingVariance): SeasonalCoachingVarianceView {
    return SeasonalCoachingVarianceView(
        code = truncate(item.code, 64),
        label = truncate(item.label, 64),
        contactEmail = item.contactEmail,
        timezone = item.timezone,
        ratioText = "%.2f".format(item.ratio),
        threshold = "%.2f".format(item.threshold),
        ownerIdText = item.ownerId,
        summaryText = truncate(item.summary, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalCoachingVarianceViews(items: MutableList<SeasonalCoachingVariance>): MutableList<SeasonalCoachingVarianceView> {
    val views = mutableListOf<SeasonalCoachingVarianceView>()
    for (item in items) {
        views.add(toSeasonalCoachingVarianceView(item))
    }
    return views
}

fun sumSeasonalCoachingVarianceView(view: SeasonalCoachingVarianceView): Double {
    val parsedRatio = view.ratioText.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedRatio + parsedThreshold
}
