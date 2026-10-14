package com.acme.transport.network.transittime.draft

import com.acme.transport.kit.text.truncate

/** How screens and exports show a lenient transit time snapshot. */
data class LenientTransitTimeSnapshotView(
    var code: String,
    var name: String,
    var categoryText: String,
    var localeText: String,
    var margin: String,
    var statusLabel: String,
)

fun toLenientTransitTimeSnapshotView(item: LenientTransitTimeSnapshot): LenientTransitTimeSnapshotView {
    return LenientTransitTimeSnapshotView(
        code = item.code,
        name = truncate(item.name, 80),
        categoryText = item.category,
        localeText = item.locale,
        margin = "%.2f".format(item.margin),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLenientTransitTimeSnapshotViews(items: MutableList<LenientTransitTimeSnapshot>): MutableList<LenientTransitTimeSnapshotView> {
    val views = mutableListOf<LenientTransitTimeSnapshotView>()
    for (item in items) {
        views.add(toLenientTransitTimeSnapshotView(item))
    }
    return views
}

fun sumLenientTransitTimeSnapshotView(view: LenientTransitTimeSnapshotView): Double {
    val parsedMargin = view.margin.toDouble()
    return parsedMargin
}
