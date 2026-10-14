package com.acme.transport.crewing.restbreak.overrideitem

/** How screens and exports show a legacy rest break mapping. */
data class LegacyRestBreakMappingView(
    var code: String,
    var titleText: String,
    var ratio: String,
    var loadKg: String,
    var revision: String,
    var name: String,
    var pinned: String,
    var batchSize: String,
    var statusLabel: String,
)

fun toLegacyRestBreakMappingView(item: LegacyRestBreakMapping): LegacyRestBreakMappingView {
    return LegacyRestBreakMappingView(
        code = item.code,
        titleText = item.title,
        ratio = "%.2f".format(item.ratio),
        loadKg = "%.2f".format(item.loadKg),
        revision = item.revision.toString(),
        name = item.name,
        pinned = "${item.pinned}",
        batchSize = item.batchSize.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyRestBreakMappingViews(items: MutableList<LegacyRestBreakMapping>): MutableList<LegacyRestBreakMappingView> {
    val views = mutableListOf<LegacyRestBreakMappingView>()
    for (item in items) {
        views.add(toLegacyRestBreakMappingView(item))
    }
    return views
}

fun sumLegacyRestBreakMappingView(view: LegacyRestBreakMappingView): Double {
    val parsedRatio = view.ratio.toDouble()
    val parsedLoadKg = view.loadKg.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedRatio + parsedLoadKg + parsedRevision.toDouble()
}
