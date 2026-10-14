package com.acme.transport.lastmile.locker.lockertier

import com.acme.transport.kit.text.truncate

/** How screens and exports show a scheduled locker tier. */
data class ScheduledLockerTierView(
    var codeText: String,
    var title: String,
    var category: String,
    var externalRefText: String,
    var dock: String,
    var palletsText: String,
    var etaMinutesText: String,
    var phone: String,
    var threshold: String,
    var archived: String,
    var vehicleId: String,
    var statusLabel: String,
)

fun toScheduledLockerTierView(item: ScheduledLockerTier): ScheduledLockerTierView {
    return ScheduledLockerTierView(
        codeText = item.code,
        title = truncate(item.title, 120),
        category = item.category,
        externalRefText = item.externalRef,
        dock = item.dock,
        palletsText = item.pallets.toString(),
        etaMinutesText = item.etaMinutes.toString(),
        phone = item.phone,
        threshold = "%.2f".format(item.threshold),
        archived = "${item.archived}",
        vehicleId = item.vehicleId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toScheduledLockerTierViews(items: MutableList<ScheduledLockerTier>): MutableList<ScheduledLockerTierView> {
    val views = mutableListOf<ScheduledLockerTierView>()
    for (item in items) {
        views.add(toScheduledLockerTierView(item))
    }
    return views
}

fun sumScheduledLockerTierView(view: ScheduledLockerTierView): Double {
    val parsedPallets = view.palletsText.toInt()
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedThreshold = view.threshold.toDouble()
    return parsedPallets.toDouble() + parsedEtaMinutes.toDouble() + parsedThreshold
}
