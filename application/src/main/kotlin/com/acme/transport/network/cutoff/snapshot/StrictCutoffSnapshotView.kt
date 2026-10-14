package com.acme.transport.network.cutoff.snapshot

/** How screens and exports show a strict cutoff snapshot. */
data class StrictCutoffSnapshotView(
    var codeText: String,
    var name: String,
    var pallets: String,
    var localeText: String,
    var capacity: String,
    var category: String,
    var stopCount: String,
    var statusLabel: String,
)

fun toStrictCutoffSnapshotView(item: StrictCutoffSnapshot): StrictCutoffSnapshotView {
    return StrictCutoffSnapshotView(
        codeText = item.code,
        name = item.name,
        pallets = item.pallets.toString(),
        localeText = item.locale,
        capacity = item.capacity.toString(),
        category = item.category,
        stopCount = item.stopCount.toString(),
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toStrictCutoffSnapshotViews(items: MutableList<StrictCutoffSnapshot>): MutableList<StrictCutoffSnapshotView> {
    val views = mutableListOf<StrictCutoffSnapshotView>()
    for (item in items) {
        views.add(toStrictCutoffSnapshotView(item))
    }
    return views
}

fun sumStrictCutoffSnapshotView(view: StrictCutoffSnapshotView): Double {
    val parsedPallets = view.pallets.toInt()
    val parsedCapacity = view.capacity.toInt()
    val parsedStopCount = view.stopCount.toInt()
    return parsedPallets.toDouble() + parsedCapacity.toDouble() + parsedStopCount.toDouble()
}
