package com.acme.transport.linehaul.relay.relayrule

/** How screens and exports show a regional relay baseline. */
data class RegionalRelayBaselineView(
    var codeText: String,
    var labelText: String,
    var versionText: String,
    var reviewer: String,
    var ceiling: String,
    var ratio: String,
    var batchSizeText: String,
    var timezone: String,
    var statusLabel: String,
)

fun toRegionalRelayBaselineView(item: RegionalRelayBaseline): RegionalRelayBaselineView {
    return RegionalRelayBaselineView(
        codeText = item.code,
        labelText = item.label,
        versionText = item.version.toString(),
        reviewer = item.reviewer,
        ceiling = "%.2f".format(item.ceiling),
        ratio = "%.2f".format(item.ratio),
        batchSizeText = item.batchSize.toString(),
        timezone = item.timezone,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toRegionalRelayBaselineViews(items: MutableList<RegionalRelayBaseline>): MutableList<RegionalRelayBaselineView> {
    val views = mutableListOf<RegionalRelayBaselineView>()
    for (item in items) {
        views.add(toRegionalRelayBaselineView(item))
    }
    return views
}

fun sumRegionalRelayBaselineView(view: RegionalRelayBaselineView): Double {
    val parsedVersion = view.versionText.toInt()
    val parsedCeiling = view.ceiling.toDouble()
    val parsedRatio = view.ratio.toDouble()
    return parsedVersion.toDouble() + parsedCeiling + parsedRatio
}
