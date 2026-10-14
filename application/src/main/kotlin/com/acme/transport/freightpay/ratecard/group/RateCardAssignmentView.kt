package com.acme.transport.freightpay.ratecard.group

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rate card assignment. */
data class RateCardAssignmentView(
    var code: String,
    var title: String,
    var amount: String,
    var ownerId: String,
    var visible: String,
    var scoreText: String,
    var driverIdText: String,
    var version: String,
    var distanceKm: String,
    var statusLabel: String,
)

fun toRateCardAssignmentView(item: RateCardAssignment): RateCardAssignmentView {
    return RateCardAssignmentView(
        code = truncate(item.code, 64),
        title = item.title,
        amount = "%.2f".format(item.amount),
        ownerId = item.ownerId,
        visible = "${item.visible}",
        scoreText = "%.2f".format(item.score),
        driverIdText = item.driverId,
        version = item.version.toString(),
        distanceKm = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRateCardAssignmentViews(items: MutableList<RateCardAssignment>): MutableList<RateCardAssignmentView> {
    val views = mutableListOf<RateCardAssignmentView>()
    for (item in items) {
        views.add(toRateCardAssignmentView(item))
    }
    return views
}

fun sumRateCardAssignmentView(view: RateCardAssignmentView): Double {
    val parsedAmount = view.amount.toDouble()
    val parsedScore = view.scoreText.toDouble()
    val parsedVersion = view.version.toInt()
    return parsedAmount + parsedScore + parsedVersion.toDouble()
}
