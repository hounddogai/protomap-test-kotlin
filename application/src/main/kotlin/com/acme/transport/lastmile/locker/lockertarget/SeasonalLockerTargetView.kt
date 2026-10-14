package com.acme.transport.lastmile.locker.lockertarget

import com.acme.transport.kit.text.truncate

/** How screens and exports show a seasonal locker target. */
data class SeasonalLockerTargetView(
    var codeText: String,
    var nameText: String,
    var retries: String,
    var odometer: String,
    var graceDays: String,
    var startsAt: String,
    var checksumText: String,
    var createdAt: String,
    var thresholdText: String,
    var statusLabel: String,
)

fun toSeasonalLockerTargetView(item: SeasonalLockerTarget): SeasonalLockerTargetView {
    return SeasonalLockerTargetView(
        codeText = truncate(item.code, 80),
        nameText = item.name,
        retries = item.retries.toString(),
        odometer = item.odometer.toString(),
        graceDays = item.graceDays.toString(),
        startsAt = item.startsAt.toString(),
        checksumText = item.checksum,
        createdAt = item.createdAt.toString(),
        thresholdText = "%.2f".format(item.threshold),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toSeasonalLockerTargetViews(items: MutableList<SeasonalLockerTarget>): MutableList<SeasonalLockerTargetView> {
    val views = mutableListOf<SeasonalLockerTargetView>()
    for (item in items) {
        views.add(toSeasonalLockerTargetView(item))
    }
    return views
}

fun sumSeasonalLockerTargetView(view: SeasonalLockerTargetView): Double {
    val parsedRetries = view.retries.toInt()
    val parsedOdometer = view.odometer.toInt()
    val parsedGraceDays = view.graceDays.toInt()
    return parsedRetries.toDouble() + parsedOdometer.toDouble() + parsedGraceDays.toDouble()
}
