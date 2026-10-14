package com.acme.transport.customs.tariff.slot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tariff slot. */
data class TariffSlotView(
    var code: String,
    var label: String,
    var priority: String,
    var timezoneText: String,
    var windowDaysText: String,
    var pinnedText: String,
    var thresholdText: String,
    var contactEmail: String,
    var statusLabel: String,
)

fun toTariffSlotView(item: TariffSlot): TariffSlotView {
    return TariffSlotView(
        code = item.code,
        label = truncate(item.label, 64),
        priority = item.priority.toString(),
        timezoneText = item.timezone,
        windowDaysText = item.windowDays.toString(),
        pinnedText = "${item.pinned}",
        thresholdText = "%.2f".format(item.threshold),
        contactEmail = item.contactEmail,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTariffSlotViews(items: MutableList<TariffSlot>): MutableList<TariffSlotView> {
    val views = mutableListOf<TariffSlotView>()
    for (item in items) {
        views.add(toTariffSlotView(item))
    }
    return views
}

fun sumTariffSlotView(view: TariffSlotView): Double {
    val parsedPriority = view.priority.toInt()
    val parsedWindowDays = view.windowDaysText.toInt()
    val parsedThreshold = view.thresholdText.toDouble()
    return parsedPriority.toDouble() + parsedWindowDays.toDouble() + parsedThreshold
}
