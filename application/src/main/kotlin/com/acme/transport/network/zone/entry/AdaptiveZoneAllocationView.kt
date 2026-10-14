package com.acme.transport.network.zone.entry

import com.acme.transport.kit.text.truncate

/** How screens and exports show a adaptive zone allocation. */
data class AdaptiveZoneAllocationView(
    var code: String,
    var name: String,
    var label: String,
    var graceDays: String,
    var source: String,
    var shiftCodeText: String,
    var revision: String,
    var loadKg: String,
    var cost: String,
    var statusLabel: String,
)

fun toAdaptiveZoneAllocationView(item: AdaptiveZoneAllocation): AdaptiveZoneAllocationView {
    return AdaptiveZoneAllocationView(
        code = item.code,
        name = item.name,
        label = item.label,
        graceDays = item.graceDays.toString(),
        source = truncate(item.source, 64),
        shiftCodeText = truncate(item.shiftCode, 64),
        revision = item.revision.toString(),
        loadKg = "%.2f".format(item.loadKg),
        cost = "%.2f".format(item.cost),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toAdaptiveZoneAllocationViews(items: MutableList<AdaptiveZoneAllocation>): MutableList<AdaptiveZoneAllocationView> {
    val views = mutableListOf<AdaptiveZoneAllocationView>()
    for (item in items) {
        views.add(toAdaptiveZoneAllocationView(item))
    }
    return views
}

fun sumAdaptiveZoneAllocationView(view: AdaptiveZoneAllocationView): Double {
    val parsedGraceDays = view.graceDays.toInt()
    val parsedRevision = view.revision.toInt()
    val parsedLoadKg = view.loadKg.toDouble()
    return parsedGraceDays.toDouble() + parsedRevision.toDouble() + parsedLoadKg
}
