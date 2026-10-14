package com.acme.transport.customs.tariff.milestone

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tariff milestone. */
data class AdaptiveTariffMilestoneView(
    var codeText: String,
    var labelText: String,
    var visible: String,
    var driverIdText: String,
    var routeCodeText: String,
    var updatedAt: String,
    var channelText: String,
    var versionText: String,
    var unitText: String,
    var durationMinutesText: String,
    var statusLabel: String,
)

fun toAdaptiveTariffMilestoneView(item: AdaptiveTariffMilestone): AdaptiveTariffMilestoneView {
    return AdaptiveTariffMilestoneView(
        codeText = truncate(item.code, 40),
        labelText = item.label,
        visible = "${item.visible}",
        driverIdText = item.driverId,
        routeCodeText = item.routeCode,
        updatedAt = item.updatedAt.toString(),
        channelText = item.channel,
        versionText = item.version.toString(),
        unitText = item.unit,
        durationMinutesText = item.durationMinutes.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTariffMilestoneViews(items: MutableList<AdaptiveTariffMilestone>): MutableList<AdaptiveTariffMilestoneView> {
    val views = mutableListOf<AdaptiveTariffMilestoneView>()
    for (item in items) {
        views.add(toAdaptiveTariffMilestoneView(item))
    }
    return views
}

fun sumAdaptiveTariffMilestoneView(view: AdaptiveTariffMilestoneView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    val parsedVersion = view.versionText.toInt()
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedUpdatedAt.toDouble() + parsedVersion.toDouble() + parsedDurationMinutes.toDouble()
}
