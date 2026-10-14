package com.acme.transport.lastmile.locker.lockerquota

/** How screens and exports show a default locker quota. */
data class DefaultLockerQuotaView(
    var code: String,
    var titleText: String,
    var etaMinutes: String,
    var windowDaysText: String,
    var priority: String,
    var odometerText: String,
    var statusLabel: String,
)

fun toDefaultLockerQuotaView(item: DefaultLockerQuota): DefaultLockerQuotaView {
    return DefaultLockerQuotaView(
        code = item.code,
        titleText = item.title,
        etaMinutes = item.etaMinutes.toString(),
        windowDaysText = item.windowDays.toString(),
        priority = item.priority.toString(),
        odometerText = item.odometer.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toDefaultLockerQuotaViews(items: MutableList<DefaultLockerQuota>): MutableList<DefaultLockerQuotaView> {
    val views = mutableListOf<DefaultLockerQuotaView>()
    for (item in items) {
        views.add(toDefaultLockerQuotaView(item))
    }
    return views
}

fun sumDefaultLockerQuotaView(view: DefaultLockerQuotaView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedPriority = view.priority.toInt()
    return parsedEtaMinutes.toDouble() + parsedWindowDays.toDouble() + parsedPriority.toDouble()
}
