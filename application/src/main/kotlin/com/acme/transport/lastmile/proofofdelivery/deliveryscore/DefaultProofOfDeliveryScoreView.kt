package com.acme.transport.lastmile.proofofdelivery.deliveryscore

import com.acme.transport.kit.text.truncate

/** How screens and exports show a default proof of delivery score. */
data class DefaultProofOfDeliveryScoreView(
    var code: String,
    var title: String,
    var minimum: String,
    var stopCount: String,
    var reviewerText: String,
    var locale: String,
    var trailerId: String,
    var archivedText: String,
    var budget: String,
    var dockText: String,
    var etaMinutes: String,
    var statusLabel: String,
)

fun toDefaultProofOfDeliveryScoreView(item: DefaultProofOfDeliveryScore): DefaultProofOfDeliveryScoreView {
    return DefaultProofOfDeliveryScoreView(
        code = item.code,
        title = item.title,
        minimum = "%.2f".format(item.minimum),
        stopCount = item.stopCount.toString(),
        reviewerText = truncate(item.reviewer, 64),
        locale = item.locale,
        trailerId = item.trailerId,
        archivedText = "${item.archived}",
        budget = "%.2f".format(item.budget),
        dockText = truncate(item.dock, 64),
        etaMinutes = item.etaMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultProofOfDeliveryScoreViews(items: MutableList<DefaultProofOfDeliveryScore>): MutableList<DefaultProofOfDeliveryScoreView> {
    val views = mutableListOf<DefaultProofOfDeliveryScoreView>()
    for (item in items) {
        views.add(toDefaultProofOfDeliveryScoreView(item))
    }
    return views
}

fun sumDefaultProofOfDeliveryScoreView(view: DefaultProofOfDeliveryScoreView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedStopCount = view.stopCount.toInt()
    val parsedBudget = view.budget.toDouble()
    return parsedMinimum + parsedStopCount.toDouble() + parsedBudget
}
