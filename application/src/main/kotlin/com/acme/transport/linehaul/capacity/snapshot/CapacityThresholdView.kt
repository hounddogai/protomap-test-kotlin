package com.acme.transport.linehaul.capacity.snapshot

import com.acme.transport.kit.text.truncate

/** How screens and exports show a capacity threshold. */
data class CapacityThresholdView(
    var codeText: String,
    var name: String,
    var region: String,
    var etaMinutes: String,
    var notesText: String,
    var statusLabel: String,
)

fun toCapacityThresholdView(item: CapacityThreshold): CapacityThresholdView {
    return CapacityThresholdView(
        codeText = item.code,
        name = item.name,
        region = item.region,
        etaMinutes = item.etaMinutes.toString(),
        notesText = truncate(item.notes, 80),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toCapacityThresholdViews(items: MutableList<CapacityThreshold>): MutableList<CapacityThresholdView> {
    val views = mutableListOf<CapacityThresholdView>()
    for (item in items) {
        views.add(toCapacityThresholdView(item))
    }
    return views
}

fun sumCapacityThresholdView(view: CapacityThresholdView): Double {
    val parsedEtaMinutes = view.etaMinutes.toInt()
    return parsedEtaMinutes.toDouble()
}
