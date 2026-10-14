package com.acme.transport.customs.hold.batch

import com.acme.transport.kit.text.truncate

/** How screens and exports show a legacy hold exception. */
data class LegacyHoldExceptionView(
    var code: String,
    var name: String,
    var depot: String,
    var notes: String,
    var sourceText: String,
    var checksum: String,
    var category: String,
    var distanceKmText: String,
    var statusLabel: String,
)

fun toLegacyHoldExceptionView(item: LegacyHoldException): LegacyHoldExceptionView {
    return LegacyHoldExceptionView(
        code = truncate(item.code, 40),
        name = item.name,
        depot = truncate(item.depot, 80),
        notes = item.notes,
        sourceText = truncate(item.source, 120),
        checksum = item.checksum,
        category = item.category,
        distanceKmText = "%.2f".format(item.distanceKm),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toLegacyHoldExceptionViews(items: MutableList<LegacyHoldException>): MutableList<LegacyHoldExceptionView> {
    val views = mutableListOf<LegacyHoldExceptionView>()
    for (item in items) {
        views.add(toLegacyHoldExceptionView(item))
    }
    return views
}

fun sumLegacyHoldExceptionView(view: LegacyHoldExceptionView): Double {
    val parsedDistanceKm = view.distanceKmText.toDouble()
    return parsedDistanceKm
}
