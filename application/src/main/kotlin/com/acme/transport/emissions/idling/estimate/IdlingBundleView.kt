package com.acme.transport.emissions.idling.estimate

/** How screens and exports show a idling bundle. */
data class IdlingBundleView(
    var code: String,
    var label: String,
    var laneText: String,
    var summary: String,
    var approved: String,
    var source: String,
    var revision: String,
    var createdAtText: String,
    var statusLabel: String,
)

fun toIdlingBundleView(item: IdlingBundle): IdlingBundleView {
    return IdlingBundleView(
        code = item.code,
        label = item.label,
        laneText = item.lane,
        summary = item.summary,
        approved = "${item.approved}",
        source = item.source,
        revision = item.revision.toString(),
        createdAtText = item.createdAt.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toIdlingBundleViews(items: MutableList<IdlingBundle>): MutableList<IdlingBundleView> {
    val views = mutableListOf<IdlingBundleView>()
    for (item in items) {
        views.add(toIdlingBundleView(item))
    }
    return views
}

fun sumIdlingBundleView(view: IdlingBundleView): Double {
    val parsedRevision = view.revision.toInt()
    val parsedCreatedAt = view.createdAtText.toInt()
    return parsedRevision.toDouble() + parsedCreatedAt.toDouble()
}
