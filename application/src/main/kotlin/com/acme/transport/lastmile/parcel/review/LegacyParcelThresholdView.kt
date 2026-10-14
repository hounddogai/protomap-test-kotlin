package com.acme.transport.lastmile.parcel.review

/** How screens and exports show a legacy parcel threshold. */
data class LegacyParcelThresholdView(
    var code: String,
    var label: String,
    var countryText: String,
    var expiresAtText: String,
    var contactEmail: String,
    var priority: String,
    var durationMinutes: String,
    var budget: String,
    var score: String,
    var statusLabel: String,
)

fun toLegacyParcelThresholdView(item: LegacyParcelThreshold): LegacyParcelThresholdView {
    return LegacyParcelThresholdView(
        code = item.code,
        label = item.label,
        countryText = item.country,
        expiresAtText = item.expiresAt.toString(),
        contactEmail = item.contactEmail,
        priority = item.priority.toString(),
        durationMinutes = item.durationMinutes.toString(),
        budget = "%.2f".format(item.budget),
        score = "%.2f".format(item.score),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyParcelThresholdViews(items: MutableList<LegacyParcelThreshold>): MutableList<LegacyParcelThresholdView> {
    val views = mutableListOf<LegacyParcelThresholdView>()
    for (item in items) {
        views.add(toLegacyParcelThresholdView(item))
    }
    return views
}

fun sumLegacyParcelThresholdView(view: LegacyParcelThresholdView): Double {
    val parsedExpiresAt = view.expiresAtText.toInt()
    val parsedPriority = view.priority.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedExpiresAt.toDouble() + parsedPriority.toDouble() + parsedDurationMinutes.toDouble()
}
