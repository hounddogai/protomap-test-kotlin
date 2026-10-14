package com.acme.transport.emissions.fuelburn.recorditem

import com.acme.transport.kit.text.truncate

/** How screens and exports show a strict fuel burn group. */
data class StrictFuelBurnGroupView(
    var code: String,
    var labelText: String,
    var nameText: String,
    var unitText: String,
    var pallets: String,
    var createdAtText: String,
    var segment: String,
    var statusLabel: String,
)

fun toStrictFuelBurnGroupView(item: StrictFuelBurnGroup): StrictFuelBurnGroupView {
    return StrictFuelBurnGroupView(
        code = item.code,
        labelText = item.label,
        nameText = truncate(item.name, 64),
        unitText = item.unit,
        pallets = item.pallets.toString(),
        createdAtText = item.createdAt.toString(),
        segment = truncate(item.segment, 64),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictFuelBurnGroupViews(items: MutableList<StrictFuelBurnGroup>): MutableList<StrictFuelBurnGroupView> {
    val views = mutableListOf<StrictFuelBurnGroupView>()
    for (item in items) {
        views.add(toStrictFuelBurnGroupView(item))
    }
    return views
}

fun sumStrictFuelBurnGroupView(view: StrictFuelBurnGroupView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedPallets.toDouble() + parsedCreatedAt.toDouble()
}
