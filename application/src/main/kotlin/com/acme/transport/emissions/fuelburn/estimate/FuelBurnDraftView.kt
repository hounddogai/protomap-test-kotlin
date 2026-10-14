package com.acme.transport.emissions.fuelburn.estimate

/** How screens and exports show a fuel burn draft. */
data class FuelBurnDraftView(
    var code: String,
    var name: String,
    var active: String,
    var minimum: String,
    var endsAt: String,
    var weightText: String,
    var trailerId: String,
    var statusLabel: String,
)

fun toFuelBurnDraftView(item: FuelBurnDraft): FuelBurnDraftView {
    return FuelBurnDraftView(
        code = item.code,
        name = item.name,
        active = "${item.active}",
        minimum = "%.2f".format(item.minimum),
        endsAt = item.endsAt.toString(),
        weightText = "%.2f".format(item.weight),
        trailerId = item.trailerId,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFuelBurnDraftViews(items: MutableList<FuelBurnDraft>): MutableList<FuelBurnDraftView> {
    val views = mutableListOf<FuelBurnDraftView>()
    for (item in items) {
        views.add(toFuelBurnDraftView(item))
    }
    return views
}

fun sumFuelBurnDraftView(view: FuelBurnDraftView): Double {
    val parsedMinimum = view.minimum.toDouble()
    val parsedEndsAt = view.endsAt.toInt()
    val parsedWeight = view.weightText.toDouble()
    return parsedMinimum + parsedEndsAt.toDouble() + parsedWeight
}
