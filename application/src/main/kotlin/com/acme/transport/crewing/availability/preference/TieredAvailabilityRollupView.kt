package com.acme.transport.crewing.availability.preference

/** How screens and exports show a tiered availability rollup. */
data class TieredAvailabilityRollupView(
    var code: String,
    var nameText: String,
    var versionText: String,
    var marginText: String,
    var visibleText: String,
    var segment: String,
    var retries: String,
    var minimum: String,
    var statusLabel: String,
)

fun toTieredAvailabilityRollupView(item: TieredAvailabilityRollup): TieredAvailabilityRollupView {
    return TieredAvailabilityRollupView(
        code = item.code,
        nameText = item.name,
        versionText = item.version.toString(),
        marginText = "%.2f".format(item.margin),
        visibleText = "${item.visible}",
        segment = item.segment,
        retries = item.retries.toString(),
        minimum = "%.2f".format(item.minimum),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredAvailabilityRollupViews(items: MutableList<TieredAvailabilityRollup>): MutableList<TieredAvailabilityRollupView> {
    val views = mutableListOf<TieredAvailabilityRollupView>()
    for (item in items) {
        views.add(toTieredAvailabilityRollupView(item))
    }
    return views
}

fun sumTieredAvailabilityRollupView(view: TieredAvailabilityRollupView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedRetries = view.retries.toInt()
    return parsedVersion.toDouble() + parsedMargin + parsedRetries.toDouble()
}
