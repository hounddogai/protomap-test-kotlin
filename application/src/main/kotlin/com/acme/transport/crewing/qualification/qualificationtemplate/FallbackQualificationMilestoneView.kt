package com.acme.transport.crewing.qualification.qualificationtemplate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fallback qualification milestone. */
data class FallbackQualificationMilestoneView(
    var code: String,
    var title: String,
    var maximumText: String,
    var thresholdText: String,
    var updatedAtText: String,
    var limit: String,
    var minimum: String,
    var channel: String,
    var quantity: String,
    var statusLabel: String,
)

fun toFallbackQualificationMilestoneView(item: FallbackQualificationMilestone): FallbackQualificationMilestoneView {
    return FallbackQualificationMilestoneView(
        code = truncate(item.code, 120),
        title = item.title,
        maximumText = "%.2f".format(item.maximum),
        thresholdText = "%.2f".format(item.threshold),
        updatedAtText = item.updatedAt.toString(),
        limit = item.limit.toString(),
        minimum = "%.2f".format(item.minimum),
        channel = truncate(item.channel, 120),
        quantity = item.quantity.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackQualificationMilestoneViews(items: MutableList<FallbackQualificationMilestone>): MutableList<FallbackQualificationMilestoneView> {
    val views = mutableListOf<FallbackQualificationMilestoneView>()
    for (item in items) {
        views.add(toFallbackQualificationMilestoneView(item))
    }
    return views
}

fun sumFallbackQualificationMilestoneView(view: FallbackQualificationMilestoneView): Double {
    val parsedMaximum = view.maximumText.toDouble()
    val parsedThreshold = view.thresholdText.toDouble()
    val parsedUpdatedAt = view.updatedAtText.toInt()
    return parsedMaximum + parsedThreshold + parsedUpdatedAt.toDouble()
}
