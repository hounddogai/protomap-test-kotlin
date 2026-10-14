package com.acme.transport.emissions.fuelburn.metric

/** How screens and exports show a tiered fuel burn slot. */
data class TieredFuelBurnSlotView(
    var code: String,
    var name: String,
    var dockText: String,
    var checksum: String,
    var version: String,
    var statusLabel: String,
)

fun toTieredFuelBurnSlotView(item: TieredFuelBurnSlot): TieredFuelBurnSlotView {
    return TieredFuelBurnSlotView(
        code = item.code,
        name = item.name,
        dockText = item.dock,
        checksum = item.checksum,
        version = item.version.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredFuelBurnSlotViews(items: MutableList<TieredFuelBurnSlot>): MutableList<TieredFuelBurnSlotView> {
    val views = mutableListOf<TieredFuelBurnSlotView>()
    for (item in items) {
        views.add(toTieredFuelBurnSlotView(item))
    }
    return views
}

fun sumTieredFuelBurnSlotView(view: TieredFuelBurnSlotView): Double {
    val parsedVersion = view.version.toInt()
    return parsedVersion.toDouble()
}
