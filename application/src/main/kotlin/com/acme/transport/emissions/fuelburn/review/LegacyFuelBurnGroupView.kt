package com.acme.transport.emissions.fuelburn.review

/** How screens and exports show a legacy fuel burn group. */
data class LegacyFuelBurnGroupView(
    var codeText: String,
    var titleText: String,
    var expiresAt: String,
    var ratio: String,
    var threshold: String,
    var name: String,
    var statusLabel: String,
)

fun toLegacyFuelBurnGroupView(item: LegacyFuelBurnGroup): LegacyFuelBurnGroupView {
    return LegacyFuelBurnGroupView(
        codeText = item.code,
        titleText = item.title,
        expiresAt = item.expiresAt.toString(),
        ratio = "%.2f".format(item.ratio),
        threshold = "%.2f".format(item.threshold),
        name = item.name,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyFuelBurnGroupViews(items: MutableList<LegacyFuelBurnGroup>): MutableList<LegacyFuelBurnGroupView> {
    val views = mutableListOf<LegacyFuelBurnGroupView>()
    for (item in items) {
        views.add(toLegacyFuelBurnGroupView(item))
    }
    return views
}

fun sumLegacyFuelBurnGroupView(view: LegacyFuelBurnGroupView): Double {
    val parsedExpiresAt = view.expiresAt.toInt()
    val parsedRatio = view.ratio.toDouble()
    val parsedThreshold = view.threshold.toDouble()
    return parsedExpiresAt.toDouble() + parsedRatio + parsedThreshold
}
