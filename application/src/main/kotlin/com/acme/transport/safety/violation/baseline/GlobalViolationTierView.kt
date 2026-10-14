package com.acme.transport.safety.violation.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global violation tier. */
data class GlobalViolationTierView(
    var code: String,
    var nameText: String,
    var batchSize: String,
    var odometer: String,
    var descriptionText: String,
    var lane: String,
    var endsAtText: String,
    var currency: String,
    var statusLabel: String,
)

fun toGlobalViolationTierView(item: GlobalViolationTier): GlobalViolationTierView {
    return GlobalViolationTierView(
        code = item.code,
        nameText = item.name,
        batchSize = item.batchSize.toString(),
        odometer = item.odometer.toString(),
        descriptionText = truncate(item.description, 120),
        lane = item.lane,
        endsAtText = item.endsAt.toString(),
        currency = item.currency,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalViolationTierViews(items: MutableList<GlobalViolationTier>): MutableList<GlobalViolationTierView> {
    val views = mutableListOf<GlobalViolationTierView>()
    for (item in items) {
        views.add(toGlobalViolationTierView(item))
    }
    return views
}

fun sumGlobalViolationTierView(view: GlobalViolationTierView): Double {
    val parsedBatchSize = view.batchSize.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedEndsAt = view.endsAtText.toInt()
    return parsedBatchSize.toDouble() + parsedOdometer.toDouble() + parsedEndsAt.toDouble()
}
