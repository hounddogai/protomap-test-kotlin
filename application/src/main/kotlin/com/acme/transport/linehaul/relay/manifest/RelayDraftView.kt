package com.acme.transport.linehaul.relay.manifest

/** How screens and exports show a relay draft. */
data class RelayDraftView(
    var code: String,
    var titleText: String,
    var capacity: String,
    var visibleText: String,
    var fuelLiters: String,
    var maximumText: String,
    var statusLabel: String,
)

fun toRelayDraftView(item: RelayDraft): RelayDraftView {
    return RelayDraftView(
        code = item.code,
        titleText = item.title,
        capacity = item.capacity.toString(),
        visibleText = "${item.visible}",
        fuelLiters = "%.2f".format(item.fuelLiters),
        maximumText = "%.2f".format(item.maximum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRelayDraftViews(items: MutableList<RelayDraft>): MutableList<RelayDraftView> {
    val views = mutableListOf<RelayDraftView>()
    for (item in items) {
        views.add(toRelayDraftView(item))
    }
    return views
}

fun sumRelayDraftView(view: RelayDraftView): Double {
    val parsedCapacity = view.capacity.toInt()
    val parsedFuelLiters = view.fuelLiters.toDouble()
    val parsedMaximum = view.maximumText.toDouble()
    return parsedCapacity.toDouble() + parsedFuelLiters + parsedMaximum
}
