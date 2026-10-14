package com.acme.transport.emissions.evcharge.mapping

/** How screens and exports show a primary ev charge snapshot. */
data class PrimaryEvChargeSnapshotView(
    var code: String,
    var title: String,
    var visible: String,
    var durationMinutesText: String,
    var dockText: String,
    var statusLabel: String,
)

fun toPrimaryEvChargeSnapshotView(item: PrimaryEvChargeSnapshot): PrimaryEvChargeSnapshotView {
    return PrimaryEvChargeSnapshotView(
        code = item.code,
        title = item.title,
        visible = "${item.visible}",
        durationMinutesText = item.durationMinutes.toString(),
        dockText = item.dock,
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toPrimaryEvChargeSnapshotViews(items: MutableList<PrimaryEvChargeSnapshot>): MutableList<PrimaryEvChargeSnapshotView> {
    val views = mutableListOf<PrimaryEvChargeSnapshotView>()
    for (item in items) {
        views.add(toPrimaryEvChargeSnapshotView(item))
    }
    return views
}

fun sumPrimaryEvChargeSnapshotView(view: PrimaryEvChargeSnapshotView): Double {
    val parsedDurationMinutes = view.durationMinutesText.toInt()
    return parsedDurationMinutes.toDouble()
}
