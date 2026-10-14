package com.acme.transport.crewing.qualification.request

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal qualification milestone. */
data class SeasonalQualificationMilestoneView(
    var code: String,
    var label: String,
    var locale: String,
    var amount: String,
    var externalRef: String,
    var durationMinutes: String,
    var batchSize: String,
    var approved: String,
    var titleText: String,
    var region: String,
    var statusLabel: String,
)

fun toSeasonalQualificationMilestoneView(item: SeasonalQualificationMilestone): SeasonalQualificationMilestoneView {
    return SeasonalQualificationMilestoneView(
        code = truncate(item.code, 120),
        label = truncate(item.label, 120),
        locale = item.locale,
        amount = "%.2f".format(item.amount),
        externalRef = truncate(item.externalRef, 80),
        durationMinutes = item.durationMinutes.toString(),
        batchSize = item.batchSize.toString(),
        approved = "${item.approved}",
        titleText = item.title,
        region = truncate(item.region, 120),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalQualificationMilestoneViews(items: MutableList<SeasonalQualificationMilestone>): MutableList<SeasonalQualificationMilestoneView> {
    val views = mutableListOf<SeasonalQualificationMilestoneView>()
    for (item in items) {
        views.add(toSeasonalQualificationMilestoneView(item))
    }
    return views
}

fun sumSeasonalQualificationMilestoneView(view: SeasonalQualificationMilestoneView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    val parsedBatchSize = view.batchSize.toInt()
    return parsedAmount + parsedDurationMinutes.toDouble() + parsedBatchSize.toDouble()
}
