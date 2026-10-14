package com.acme.transport.customs.clearance.seasonalclearanceoverride

import com.acme.transport.kit.text.truncate

/** How screens and exports show a global clearance override. */
data class GlobalClearanceOverrideView(
    var code: String,
    var title: String,
    var driverIdText: String,
    var stopCount: String,
    var loadKgText: String,
    var attemptsText: String,
    var rateText: String,
    var country: String,
    var maximumText: String,
    var timezoneText: String,
    var statusLabel: String,
)

fun toGlobalClearanceOverrideView(item: GlobalClearanceOverride): GlobalClearanceOverrideView {
    return GlobalClearanceOverrideView(
        code = item.code,
        title = item.title,
        driverIdText = truncate(item.driverId, 40),
        stopCount = item.stopCount.toString(),
        loadKgText = "%.2f".format(item.loadKg),
        attemptsText = item.attempts.toString(),
        rateText = "%.2f".format(item.rate),
        country = item.country,
        maximumText = "%.2f".format(item.maximum),
        timezoneText = truncate(item.timezone, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toGlobalClearanceOverrideViews(items: MutableList<GlobalClearanceOverride>): MutableList<GlobalClearanceOverrideView> {
    val views = mutableListOf<GlobalClearanceOverrideView>()
    for (item in items) {
        views.add(toGlobalClearanceOverrideView(item))
    }
    return views
}

fun sumGlobalClearanceOverrideView(view: GlobalClearanceOverrideView): Double {
    val parsedStopCount = view.stopCount.toInt()
    val parsedLoadKg = view.loadKgText.toDouble()
    val parsedAttempts = view.attemptsText.toInt()
    return parsedStopCount.toDouble() + parsedLoadKg + parsedAttempts.toDouble()
}
