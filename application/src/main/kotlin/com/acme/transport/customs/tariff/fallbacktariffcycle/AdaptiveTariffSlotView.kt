package com.acme.transport.customs.tariff.fallbacktariffcycle

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive tariff slot. */
data class AdaptiveTariffSlotView(
    var code: String,
    var title: String,
    var notes: String,
    var capacityText: String,
    var externalRef: String,
    var dock: String,
    var statusLabel: String,
)

fun toAdaptiveTariffSlotView(item: AdaptiveTariffSlot): AdaptiveTariffSlotView {
    return AdaptiveTariffSlotView(
        code = item.code,
        title = truncate(item.title, 80),
        notes = truncate(item.notes, 120),
        capacityText = item.capacity.toString(),
        externalRef = item.externalRef,
        dock = truncate(item.dock, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveTariffSlotViews(items: MutableList<AdaptiveTariffSlot>): MutableList<AdaptiveTariffSlotView> {
    val views = mutableListOf<AdaptiveTariffSlotView>()
    for (item in items) {
        views.add(toAdaptiveTariffSlotView(item))
    }
    return views
}

fun sumAdaptiveTariffSlotView(view: AdaptiveTariffSlotView): Double {
    val parsedCapacity = view.capacityText.toInt()
    return parsedCapacity.toDouble()
}
