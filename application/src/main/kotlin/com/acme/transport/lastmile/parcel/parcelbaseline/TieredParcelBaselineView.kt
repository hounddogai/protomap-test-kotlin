package com.acme.transport.lastmile.parcel.parcelbaseline

import com.acme.transport.kit.text.truncate

/** How screens and exports show a tiered parcel baseline. */
data class TieredParcelBaselineView(
    var code: String,
    var title: String,
    var updatedAt: String,
    var trailerIdText: String,
    var shiftCode: String,
    var statusLabel: String,
)

fun toTieredParcelBaselineView(item: TieredParcelBaseline): TieredParcelBaselineView {
    return TieredParcelBaselineView(
        code = item.code,
        title = truncate(item.title, 80),
        updatedAt = item.updatedAt.toString(),
        trailerIdText = item.trailerId,
        shiftCode = item.shiftCode,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toTieredParcelBaselineViews(items: MutableList<TieredParcelBaseline>): MutableList<TieredParcelBaselineView> {
    val views = mutableListOf<TieredParcelBaselineView>()
    for (item in items) {
        views.add(toTieredParcelBaselineView(item))
    }
    return views
}

fun sumTieredParcelBaselineView(view: TieredParcelBaselineView): Double {
    val parsedUpdatedAt = view.updatedAt.toInt()
    return parsedUpdatedAt.toDouble()
}
