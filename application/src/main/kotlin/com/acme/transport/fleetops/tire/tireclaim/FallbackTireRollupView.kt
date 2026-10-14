package com.acme.transport.fleetops.tire.tireclaim

/** How screens and exports show a fallback tire rollup. */
data class FallbackTireRollupView(
    var codeText: String,
    var title: String,
    var limit: String,
    var archived: String,
    var sourceText: String,
    var marginText: String,
    var depot: String,
    var trailerId: String,
    var revision: String,
    var approved: String,
    var statusLabel: String,
)

fun toFallbackTireRollupView(item: FallbackTireRollup): FallbackTireRollupView {
    return FallbackTireRollupView(
        codeText = item.code,
        title = item.title,
        limit = item.limit.toString(),
        archived = "${item.archived}",
        sourceText = item.source,
        marginText = "%.2f".format(item.margin),
        depot = item.depot,
        trailerId = item.trailerId,
        revision = item.revision.toString(),
        approved = "${item.approved}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toFallbackTireRollupViews(items: MutableList<FallbackTireRollup>): MutableList<FallbackTireRollupView> {
    val views = mutableListOf<FallbackTireRollupView>()
    for (item in items) {
        views.add(toFallbackTireRollupView(item))
    }
    return views
}

fun sumFallbackTireRollupView(view: FallbackTireRollupView): Double {
    val parsedLimit = view.limit.toInt()
    val parsedMargin = view.marginText.toDouble()
    val parsedRevision = view.revision.toInt()
    return parsedLimit.toDouble() + parsedMargin + parsedRevision.toDouble()
}
