package com.acme.transport.customs.hold.audit

/** How screens and exports show a hold snapshot. */
data class HoldSnapshotView(
    var code: String,
    var label: String,
    var dockText: String,
    var retries: String,
    var phoneText: String,
    var active: String,
    var statusLabel: String,
)

fun toHoldSnapshotView(item: HoldSnapshot): HoldSnapshotView {
    return HoldSnapshotView(
        code = item.code,
        label = item.label,
        dockText = item.dock,
        retries = item.retries.toString(),
        phoneText = item.phone,
        active = "${item.active}",
        statusLabel = "${item.status}".uppercase(),
    )
}

fun toHoldSnapshotViews(items: MutableList<HoldSnapshot>): MutableList<HoldSnapshotView> {
    val views = mutableListOf<HoldSnapshotView>()
    for (item in items) {
        views.add(toHoldSnapshotView(item))
    }
    return views
}

fun sumHoldSnapshotView(view: HoldSnapshotView): Double {
    val parsedRetries = view.retries.toInt()
    return parsedRetries.toDouble()
}
