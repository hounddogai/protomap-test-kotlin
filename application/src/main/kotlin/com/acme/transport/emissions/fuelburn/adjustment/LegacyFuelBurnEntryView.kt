package com.acme.transport.emissions.fuelburn.adjustment

/** How screens and exports show a legacy fuel burn entry. */
data class LegacyFuelBurnEntryView(
    var code: String,
    var name: String,
    var region: String,
    var endsAtText: String,
    var timezone: String,
    var archived: String,
    var durationMinutes: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toLegacyFuelBurnEntryView(item: LegacyFuelBurnEntry): LegacyFuelBurnEntryView {
    return LegacyFuelBurnEntryView(
        code = item.code,
        name = item.name,
        region = item.region,
        endsAtText = item.endsAt.toString(),
        timezone = item.timezone,
        archived = "${item.archived}",
        durationMinutes = item.durationMinutes.toString(),
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyFuelBurnEntryViews(items: MutableList<LegacyFuelBurnEntry>): MutableList<LegacyFuelBurnEntryView> {
    val views = mutableListOf<LegacyFuelBurnEntryView>()
    for (item in items) {
        views.add(toLegacyFuelBurnEntryView(item))
    }
    return views
}

fun sumLegacyFuelBurnEntryView(view: LegacyFuelBurnEntryView): Double {
    val parsedEndsAt = view.endsAtText.toInt()
    val parsedDurationMinutes = view.durationMinutes.toInt()
    return parsedEndsAt.toDouble() + parsedDurationMinutes.toDouble()
}
