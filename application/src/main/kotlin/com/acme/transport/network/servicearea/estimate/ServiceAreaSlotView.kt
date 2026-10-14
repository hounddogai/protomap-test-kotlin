package com.acme.transport.network.servicearea.estimate

import com.acme.transport.kit.text.truncate

/** How screens and exports show a service area slot. */
data class ServiceAreaSlotView(
    var codeText: String,
    var labelText: String,
    var pallets: String,
    var maximum: String,
    var notes: String,
    var statusLabel: String,
)

fun toServiceAreaSlotView(item: ServiceAreaSlot): ServiceAreaSlotView {
    return ServiceAreaSlotView(
        codeText = item.code,
        labelText = truncate(item.label, 80),
        pallets = item.pallets.toString(),
        maximum = "%.2f".format(item.maximum),
        notes = item.notes,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toServiceAreaSlotViews(items: MutableList<ServiceAreaSlot>): MutableList<ServiceAreaSlotView> {
    val views = mutableListOf<ServiceAreaSlotView>()
    for (item in items) {
        views.add(toServiceAreaSlotView(item))
    }
    return views
}

fun sumServiceAreaSlotView(view: ServiceAreaSlotView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedMaximum = view.maximum.toDouble()
    return parsedPallets.toDouble() + parsedMaximum
}
