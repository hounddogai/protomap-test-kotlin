package com.acme.transport.safety.nearmiss.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a rolling near miss adjustment. */
data class RollingNearMissAdjustmentView(
    var code: String,
    var label: String,
    var channelText: String,
    var scoreText: String,
    var externalRef: String,
    var dockText: String,
    var vehicleId: String,
    var distanceKmText: String,
    var budgetText: String,
    var phoneText: String,
    var statusLabel: String,
)

fun toRollingNearMissAdjustmentView(item: RollingNearMissAdjustment): RollingNearMissAdjustmentView {
    return RollingNearMissAdjustmentView(
        code = item.code,
        label = truncate(item.label, 64),
        channelText = item.channel,
        scoreText = "%.2f".format(item.score),
        externalRef = item.externalRef,
        dockText = item.dock,
        vehicleId = item.vehicleId,
        distanceKmText = "%.2f".format(item.distanceKm),
        budgetText = "%.2f".format(item.budget),
        phoneText = item.phone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRollingNearMissAdjustmentViews(items: MutableList<RollingNearMissAdjustment>): MutableList<RollingNearMissAdjustmentView> {
    val views = mutableListOf<RollingNearMissAdjustmentView>()
    for (item in items) {
        views.add(toRollingNearMissAdjustmentView(item))
    }
    return views
}

fun sumRollingNearMissAdjustmentView(view: RollingNearMissAdjustmentView): Double {
    val parsedScore = view.scoreText.toDouble()
    val parsedDistanceKm = view.distanceKmText.toDouble()
    val parsedBudget = view.budgetText.toDouble()
    return parsedScore + parsedDistanceKm + parsedBudget
}
