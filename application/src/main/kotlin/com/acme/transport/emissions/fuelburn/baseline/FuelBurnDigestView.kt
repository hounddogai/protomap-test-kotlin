package com.acme.transport.emissions.fuelburn.baseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a fuel burn digest. */
data class FuelBurnDigestView(
    var code: String,
    var name: String,
    var ownerId: String,
    var approved: String,
    var activeText: String,
    var labelText: String,
    var weightText: String,
    var statusLabel: String,
)

fun toFuelBurnDigestView(item: FuelBurnDigest): FuelBurnDigestView {
    return FuelBurnDigestView(
        code = item.code,
        name = truncate(item.name, 40),
        ownerId = truncate(item.ownerId, 64),
        approved = "${item.approved}",
        activeText = "${item.active}",
        labelText = truncate(item.label, 120),
        weightText = "%.2f".format(item.weight),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnDigestViews(items: MutableList<FuelBurnDigest>): MutableList<FuelBurnDigestView> {
    val views = mutableListOf<FuelBurnDigestView>()
    for (item in items) {
        views.add(toFuelBurnDigestView(item))
    }
    return views
}

fun sumFuelBurnDigestView(view: FuelBurnDigestView): Double {
    val parsedWeight = view.weightText.toDouble()
    return parsedWeight
}
