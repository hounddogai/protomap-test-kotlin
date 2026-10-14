package com.acme.transport.customs.tariff.schedule

import com.acme.transport.kit.text.truncate

/** How screens and exports show a manual tariff schedule. */
data class ManualTariffScheduleView(
    var codeText: String,
    var name: String,
    var laneText: String,
    var etaMinutesText: String,
    var ratio: String,
    var priority: String,
    var visible: String,
    var approvedText: String,
    var palletsText: String,
    var ownerId: String,
    var revision: String,
    var statusLabel: String,
)

fun toManualTariffScheduleView(item: ManualTariffSchedule): ManualTariffScheduleView {
    return ManualTariffScheduleView(
        codeText = item.code,
        name = item.name,
        laneText = item.lane,
        etaMinutesText = item.etaMinutes.toString(),
        ratio = "%.2f".format(item.ratio),
        priority = item.priority.toString(),
        visible = "${item.visible}",
        approvedText = "${item.approved}",
        palletsText = item.pallets.toString(),
        ownerId = truncate(item.ownerId, 40),
        revision = item.revision.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toManualTariffScheduleViews(items: MutableList<ManualTariffSchedule>): MutableList<ManualTariffScheduleView> {
    val views = mutableListOf<ManualTariffScheduleView>()
    for (item in items) {
        views.add(toManualTariffScheduleView(item))
    }
    return views
}

fun sumManualTariffScheduleView(view: ManualTariffScheduleView): Double {
    val parsedEtaMinutes = view.etaMinutesText.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedPriority = view.priority.toInt()
    return parsedEtaMinutes.toDouble() + parsedRatio + parsedPriority.toDouble()
}
